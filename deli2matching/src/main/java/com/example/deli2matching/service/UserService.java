package com.example.deli2matching.service;


import com.example.deli2matching.dao.UserDao;
import com.example.deli2matching.dto.user.MyInfoReqDTO;
import com.example.deli2matching.entity.user.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserDao userDao;

    public UserEntity create(final UserEntity userEntity) {
        // 중복 loginId 검사
        final String loginId = userEntity.getLoginId();
        if (userDao.existsByLoginId(loginId)) {
            log.warn("LoginId already exists {}", loginId);
            throw new RuntimeException("LoginId already exists");
        }

        // 중복 nickname 검사
        final String nickname = userEntity.getNickname();
        if (userDao.nameExists(nickname) > 0) {
            log.warn("Nickname already exists {}", nickname);
            throw new RuntimeException("Nickname already exists");
        }

        userDao.insert(userEntity);

        return userEntity; // id가 채워진 상태로 반환
    }//

    public UserEntity getByCredentials(final String loginId, final String password,
                                       final PasswordEncoder encoder) {
        // loginId로 DB에서 사용자 조회
        final UserEntity originalUser = userDao.findByLoginId(loginId);

        System.out.println(password);

        log.info("loginId: {}, password: {}", loginId, password);
        log.info("originalUser: {}", originalUser);
        log.info("encoder.matches(password, originalUser.getPassword()): {}", encoder.matches(password, originalUser.getPassword()));

        // 사용자가 있고 비밀번호가 일치하면 반환
        if (originalUser != null && encoder.matches(password, originalUser.getPassword())) {
            return originalUser;
        }

        // 사용자 없거나 비밀번호 불일치 → null 반환 (컨트롤러에서 에러 처리)
        return null;
    }

    // 회원가입 아이디 중복체크
    public int idExists(String memberId) {
        return userDao.idExists(memberId);
    }//

    // 회원가입 닉네임 중복체크
    public int nameExists(String memberName) {
        return userDao.nameExists(memberName);
    }//

    public String findLoginIdByUserId(String userId) {
        return userDao.findLoginIdByUserId(userId);
    }//

    public UserEntity getMyInfo(long userId) {
        return userDao.findByUserId(userId);
    }//

    public void updateMyInfo(MyInfoReqDTO req) {
        userDao.updateMyInfo(req);
    }//
}
