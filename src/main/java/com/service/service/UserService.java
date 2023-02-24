package com.service.service;

import com.service.api.form.UserForm;
import com.service.api.response.PageResponse;
import com.service.config.Translator;
import com.service.dto.UserDTO;
import com.service.exception.ResourceNotFoundException;
import com.service.model.AppUser;
import com.service.model.Token;
import com.service.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "USER-SERVICE")
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private MailService mailService;
    @Autowired
    private PasswordEncoder encoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepo.findByUsername(username);
        if (appUser == null) {
            log.info("User not found in the database");
            throw new UsernameNotFoundException(Translator.toLocale("user-name-not-found"));
        } else {
            log.info("User found in the database {}", username);
        }
        return new org.springframework.security.core.userdetails.User(appUser.getUsername(), appUser.getPassword(), getAuthority(username));
    }

    /**
     * Get user authorities
     *
     * @param username username
     * @return list of authorities
     */
    private List<SimpleGrantedAuthority> getAuthority(String username) {
        List<String> roles = userRepo.findRoleByUsername(username);
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    /**
     * Get all users
     *
     * @param pageNo   page number
     * @param pageSize page size
     * @return list of user
     */
    public PageResponse findAll(int pageNo, int pageSize) {
        log.info("Fetching all users from the database");

        if (pageNo > 0) pageNo = pageNo - 1;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<AppUser> users = userRepo.findAll(pageable);
        List<UserDTO> userDTOs = users.getContent().stream().map(x -> UserDTO.builder()
                .username(x.getUsername())
                .password(x.getPassword())
                .build()).collect(Collectors.toList());
        PageResponse response = PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .total(users.getTotalElements())
                .items(userDTOs)
                .build();

        return response;
    }

    /**
     * @param keyword  key search
     * @param pageNo   page number
     * @param pageSize page size
     * @return list of user
     */
    public PageResponse searchByName(String keyword, int pageNo, int pageSize) {
        log.info("Searching user from the database");

        if (pageNo > 0) pageNo = pageNo - 1;
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<AppUser> users = userRepo.searchByName(keyword, pageable);
        List<UserDTO> userDTOs = users.getContent().stream().map(x -> UserDTO.builder()
                .username(x.getUsername())
                .password(x.getPassword())
                .build()).collect(Collectors.toList());

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .total(users.getTotalElements())
                .items(userDTOs)
                .build();
    }

    /**
     * Get user by id
     *
     * @param id user id
     * @return user
     * @throws ResourceNotFoundException not found exception
     */
    public AppUser getUserById(long id) throws ResourceNotFoundException {
        log.info("Fetching user {} from the database", id);
        Optional<AppUser> user = Optional.ofNullable(userRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(Translator.toLocale("user-id-not-found"))));
        return user.get();
    }

    /**
     * Get user by email
     *
     * @param email email
     * @return user
     * @throws ResourceNotFoundException not found exception
     */
    public AppUser getByEmail(String email) throws ResourceNotFoundException {
        log.info("Fetching user {} from the database", email);

        AppUser user = userRepo.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException(Translator.toLocale("user-email-not-found"));
        }
        return user;
    }

    /**
     * Save or update user
     *
     * @param form
     * @return
     * @throws ResourceNotFoundException
     */
    public long saveOrUpdate(UserForm form) throws ResourceNotFoundException {
        log.info("Saving user {} to the database", form.getUsername());

        if (Objects.isNull(form.getId())) {
            return userRepo.save(AppUser.builder()
                    .email(form.getEmail())
                    .username(form.getUsername())
                    .password(encoder.encode(form.getPassword()))
                    .createdDate(new Date())
                    .build()).getId();
        } else {
            AppUser user = getUserById(form.getId());
            if (!Objects.isNull(form.getUsername())) {
                user.setUsername(form.getUsername());
            }
            if (!Objects.isNull(form.getPassword())) {
                user.setPassword(encoder.encode(form.getPassword()));
            }
            return userRepo.save(user).getId();
        }
    }

    /**
     * Change password
     *
     * @param id
     * @param password
     * @throws ResourceNotFoundException
     */
    public long changePassword(Long id, String password) throws ResourceNotFoundException {
        log.info("Changing password for user {} to the database", id);

        AppUser user = getUserById(id);
        if (!Objects.isNull(password)) {
            user.setPassword(encoder.encode(password));
        }
        return userRepo.save(user).getId();
    }

    /**
     * Delete terminate user
     *
     * @param id user id
     * @throws ResourceNotFoundException not found exception
     */
    public boolean delete(Long id) throws ResourceNotFoundException {
        log.info("Deleting user {} from the database", id);

        if (Objects.nonNull(id)) {
            userRepo.deleteById(id);
            return true;
        }

        return false;
    }


    /**
     * Send link confirm in order to reset password token to email
     *
     * @param email user's email
     * @throws ResourceNotFoundException
     */
    public void sendResetTokenToEmail(String email) throws ResourceNotFoundException {
        // get user by email
        AppUser appUser = getByEmail(email);

        // reset password into database
        String resetToken = RandomStringUtils.randomNumeric(10);

        // Todo: replace domain name in order to navigate to change password page
        String resetLink = "http://domain.name/reset-password?token=" + resetToken;

        // send new password to email
        // Todo: Need to replace email content
        String from = "no-reply@service.api";
        String subject = "[API-Service] Reset password request";
        String body = String.format("Dear %s,\n\n" +
                "If your forgot password\n" +
                "Click link to reset password: %s\n\n" +
                "- BackEnd Team", appUser.getUsername(), resetLink);

        // send mail to user
        mailService.sendEmail(from, email, subject, body);

        // save reset password token into the database
        tokenService.saveResetToken(appUser, resetToken);
    }

    /**
     * Reset old password and update new password
     *
     * @param resetToken  reset token
     * @param newPassword new password
     */
    public void resetAndUpdatePassword(String resetToken, String newPassword) throws ResourceNotFoundException {
        Token token = tokenService.findByToken(resetToken);
        if (token == null) {
            throw new ResourceNotFoundException(Translator.toLocale("reset-token-not-found"));
        }
        if (token.getExpiryDate().before(new Date())) {
            throw new ResourceNotFoundException(Translator.toLocale("reset-token-expired"));
        }

        // Update new password
        AppUser appUser = token.getUser();
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        appUser.setPassword(encoder.encode(newPassword));
        userRepo.save(appUser);
    }

}
