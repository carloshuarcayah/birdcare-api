package pe.com.birdcare.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.com.birdcare.entity.User;
import pe.com.birdcare.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class  UserDetailsServiceImp implements UserDetailsService {

    private final UserRepository userRepository;

    @Override @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("User not found with email: "+email));
        return new AppUserDetails(user);
    }
}