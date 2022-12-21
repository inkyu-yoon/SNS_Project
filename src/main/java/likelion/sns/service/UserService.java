package likelion.sns.service;


import likelion.sns.Exception.ErrorCode;
import likelion.sns.Exception.SNSAppException;
import likelion.sns.domain.dto.join.UserJoinRequestDto;
import likelion.sns.domain.dto.join.UserJoinResponseDto;
import likelion.sns.domain.entity.User;
import likelion.sns.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public UserJoinResponseDto createUser(UserJoinRequestDto userJoinRequestDto) {
        userRepository.findByUserName(userJoinRequestDto.getUserName()).ifPresent(user -> {
            throw new SNSAppException(ErrorCode.DUPLICATED_USER_NAME, userJoinRequestDto.getUserName()+"는(은) 이미 존재합니다.");
        });
        String password = encoder.encode(userJoinRequestDto.getPassword());

        User saved = userRepository.save(userJoinRequestDto.toEntity(password));
        return new UserJoinResponseDto(saved);
    }

}
