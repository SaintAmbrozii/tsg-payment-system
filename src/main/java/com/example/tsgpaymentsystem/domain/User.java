package com.example.tsgpaymentsystem.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.management.relation.Role;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email"})
})
public class User implements UserDetails, Serializable {

  private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "lastname")
    private String lastname;


    @Column(name = "email",unique = true)
    private String email;

    @Column(name = "password")
    private String password;
    @Column(name = "address")
    private String address;
    @Column(name = "contract")
    private String contract;
    @Column(unique = true,  length = 20)
    private String phone;

    private Boolean isAktive;

    private Boolean api;

    private Boolean isAggregated;

    private Long lastUpload;

 //  @Enumerated(EnumType.STRING)
  // @ElementCollection(targetClass = Role.class,fetch = FetchType.EAGER)
 ////  @CollectionTable(name = "user_role",joinColumns = @JoinColumn(name = "customer_id"))
  // private Set<Roles> roles = new HashSet<>();

   @Enumerated(EnumType.STRING)
   private Roles role;

   private ZonedDateTime lastUploadDate;




    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + role +
                ", id=" + id +
                '}';
    }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return isAktive;
  }

  @Override
  public boolean isAccountNonLocked() {
    return isAktive;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return isAktive;
  }

  @Override
  public boolean isEnabled() {
    return isAktive;
  }
}
