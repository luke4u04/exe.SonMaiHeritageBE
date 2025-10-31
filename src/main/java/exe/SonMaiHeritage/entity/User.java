package exe.SonMaiHeritage.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name="users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;
    
    @Column(name="username", unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
    private String username;
    
    @Column(name="email", unique = true, nullable = false, columnDefinition = "VARCHAR(255)")
    private String email;
    
    @Column(name="password", nullable = false)
    private String password;
    
    @Column(name="first_name", columnDefinition = "VARCHAR(255)")
    private String firstName;
    
    @Column(name="last_name", columnDefinition = "VARCHAR(255)")
    private String lastName;
    
    @Column(name="phone", columnDefinition = "VARCHAR(20)")
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(name="role")
    @Builder.Default
    private Role role = Role.USER;
    
    @Column(name="enabled")
    @Builder.Default
    private Boolean enabled = true;
    
    @Column(name="account_non_expired")
    @Builder.Default
    private Boolean accountNonExpired = true;
    
    @Column(name="account_non_locked")
    @Builder.Default
    private Boolean accountNonLocked = true;
    
    @Column(name="credentials_non_expired")
    @Builder.Default
    private Boolean credentialsNonExpired = true;

    // UserDetails interface methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // Custom method to get full name
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username; // Fallback to username if no names are set
        }
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Address> addresses;

    public enum Role {
        USER, ADMIN
    }
}
