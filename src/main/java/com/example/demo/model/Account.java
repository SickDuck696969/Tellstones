package com.example.demo.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "account")
public class Account implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String avatar; // URL or path to avatar image
    private String displayName;
    private Integer credit = 0;
    private Integer fragment = 0;

    @ElementCollection
    private List<String> matchHistory;

    private Boolean isAccountNonExpired = true;
    private Boolean isAccountNonLocked = true;
    private Boolean isCredentialsNonExpired = true;
    private Boolean isEnabled = true;

    public Account() {
    }

    public Account(Long id, String username, String password, String email, String avatar, String displayName,
            Integer credit, List<String> matchHistory, Boolean isAccountNonExpired, Boolean isAccountNonLocked,
            Boolean isCredentialsNonExpired, Boolean isEnabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.avatar = avatar;
        this.displayName = displayName;
        this.credit = credit;
        this.matchHistory = matchHistory;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getCredit() { return credit; }
    public void setCredit(Integer credit) { this.credit = credit; }

    public Integer getFragment() { return fragment; }
    public void setFragment(Integer fragment) { this.fragment = fragment; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public List<String> getMatchHistory() { return matchHistory; }
    public void setMatchHistory(List<String> matchHistory) { this.matchHistory = matchHistory; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    @Override
    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    @Override
    public boolean isAccountNonExpired() { return isAccountNonExpired; }

    public void setAccountNonExpired(Boolean accountNonExpired) { isAccountNonExpired = accountNonExpired; }

    @Override
    public boolean isAccountNonLocked() { return isAccountNonLocked; }

    public void setAccountNonLocked(Boolean accountNonLocked) { isAccountNonLocked = accountNonLocked; }

    @Override
    public boolean isCredentialsNonExpired() { return isCredentialsNonExpired; }

    public void setCredentialsNonExpired(Boolean credentialsNonExpired) { isCredentialsNonExpired = credentialsNonExpired; }

    @Override
    public boolean isEnabled() { return isEnabled; }

    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
}
