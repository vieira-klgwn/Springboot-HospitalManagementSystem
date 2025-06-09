package vector.HospitalManagementApplication.models;



import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;


import java.util.*;
import java.util.stream.Collectors;

import static vector.HospitalManagementApplication.models.Permission.*;


@RequiredArgsConstructor
public enum Role {
    USER(Collections.emptySet()),
    ADMIN(
            Set.of(ADMIN_READ,
                    ADMIN_CREATE,
                    ADMIN_UPDATE,
                    ADMIN_DELETE,
                    MANAGER_READ,
                    MANAGER_DELETE,
                    MANAGER_UPDATE,
                    MANAGER_CREATE
            )
    ),
    MANAGER(
            Set.of(
                    MANAGER_READ,
                    MANAGER_CREATE,
                    MANAGER_UPDATE,
                    MANAGER_DELETE
            )
    ),
    TEAM_LEAD(
            Set.of(
                    MANAGER_READ,
                    MANAGER_CREATE,
                    MANAGER_UPDATE,
                    MANAGER_DELETE,
                    TEAM_LEAD_CREATE,
                    TEAM_LEAD_ASSIGN,
                    TEAM_LEAD_ADD_MEMBER


    ));

    @Getter
    private final Set<Permission> permissions;



    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + name()));
        return authorities;
    }


}
