package com.example.deli2matching.service;


import com.example.deli2matching.dao.UserDao;
import com.example.deli2matching.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserDao userDao;

    /**
     * create - 신규 사용자 회원가입
     *
     * 처리 순서:
     *  1. 입력값 유효성 검사
     *  2. 중복 loginId 검사
     *  3. DB에 INSERT
     *  4. 저장된 사용자(id가 세팅된) 반환
     *
     * @param userEntity 저장할 사용자 정보 (비밀번호는 이미 암호화된 상태)
     * @return DB에 저장된 사용자 (id가 채워진 상태)
     * @throws RuntimeException 유효하지 않은 입력이거나 loginId 중복 시
     */
    public UserEntity create(final UserEntity userEntity) {
        // 필수 정보 유효성 검사
        if (userEntity == null || userEntity.getLoginId() == null) {
            throw new RuntimeException("Invalid arguments");
        }

        final String loginId = userEntity.getLoginId();

        // 중복 loginId 검사
        if (userDao.existsByLoginId(loginId)) {
            log.warn("LoginId already exists {}", loginId);
            throw new RuntimeException("LoginId already exists");
        }

        // MyBatis INSERT 실행
        // UserMapper.xml의 useGeneratedKeys="true" 덕분에
        // INSERT 후 DB가 자동 생성한 id가 userEntity.id에 자동으로 세팅됨
        userDao.insert(userEntity);
        return userEntity; // id가 채워진 상태로 반환
    }

    /**
     * getByCredentials - 로그인 인증 처리
     *
     * loginId 로 사용자를 찾고, 입력한 비밀번호가 DB의 암호화된 비밀번호와 일치하는지 확인합니다.
     *
     * @param loginId 로그인 시도하는 사용자
     * @param password 입력한 평문 비밀번호
     * @param encoder  BCryptPasswordEncoder
     * @return 인증 성공 시 UserEntity, 실패 시 null
     */
    public UserEntity getByCredentials(final String loginId, final String password,
                                       final PasswordEncoder encoder) {
        // loginId로 DB에서 사용자 조회
        final UserEntity originalUser = userDao.findByLoginId(loginId);

        // 사용자가 있고 비밀번호가 일치하면 반환
        if (originalUser != null && encoder.matches(password, originalUser.getPassword())) {
            return originalUser;
        }

        // 사용자 없거나 비밀번호 불일치 → null 반환 (컨트롤러에서 에러 처리)
        return null;
    }

}
