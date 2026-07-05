import { Link, useNavigate } from "react-router-dom";
import axiosInstance from "../../utils/axios";
import useAuthStore from "../../utils/useAuthStore";
import { useState } from "react";
import styles from "./Login.module.css";
import kakaoLogo from "../../assets/logo/kakao_logo.png";
import googleLogo from "../../assets/logo/google_logo.png";
import naverLogo from "../../assets/logo/naver_logo.png";
import githubLogo from "../../assets/logo/github_logo.png";

const Login = () => {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);

  const inputMember = (e) => {
    const newMember = { ...member, [e.target.name]: e.target.value };
    setMember(newMember);
  };

  const [member, setMember] = useState({ memberId: "", memberPw: "" });

  const handleSubmit = (event) => {
    event.preventDefault();

    console.log(JSON.stringify(member));
    axiosInstance
      .post("/auth/signin", member)
      .then((response) => {
        if (response.data.token) {
          console.log(response.data);
          login(response.data);
          navigate("/");
        }
      })
      .catch((err) => {
        console.log(err);
      });
  };

  const handleSocialLogin = (provider) => {
    // window.location.href: 현재 브라우저를 다른 주소로 이동
    // VITE_API_BASE_URL: 백엔드 서버
    // /oauth2/authorization/: Spring Security OAuth2가 제공하는 URL
    // provider: 로그인 제공자
    // redirect_url: 최종적으로 다시 돌아올 프론트 주소
    // window.location.origin: 현재 프론트 주소 (protocol + host => http://localhost:5173)

    window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/${provider}?redirect_url=${encodeURIComponent(window.location.origin)}`;
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <h1 className={styles.title}>로그인</h1>

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.fieldGroup}>
            <label htmlFor="memberId" className={styles.label}>
              아이디
            </label>
            <div className={styles.inputWrapper}>
              <input
                type="text"
                name="memberId"
                id="memberId"
                value={member.memberId}
                onChange={inputMember}
                className={styles.input}
              />
            </div>
          </div>

          <div className={styles.fieldGroup}>
            <label htmlFor="memberPw" className={styles.label}>
              비밀번호
            </label>
            <div className={styles.inputWrapper}>
              <input
                type="password"
                name="memberPw"
                id="memberPw"
                placeholder="비밀번호를 입력하세요"
                value={member.memberPw}
                onChange={inputMember}
                className={styles.input}
              />
            </div>
          </div>

          <button type="submit" className={styles.loginBtn}>
            로그인
          </button>

          <div className={styles.testAccount}>
            <p className={styles.testAccount_title}>테스트 계정</p>
            <div className={styles.testAccount_item}>
              <span>ID: qqqq1111!</span>
              <span className={styles.testAccount_sep}>/</span>
              <span>PW: qqqq1111!</span>
            </div>
            <div className={styles.testAccount_item}>
              <span>ID: wwww2222@</span>
              <span className={styles.testAccount_sep}>/</span>
              <span>PW: wwww2222@</span>
            </div>
          </div>

          <div className={styles.signupRow}>
            <Link to="/signup" className={styles.signupLink}>
              회원가입
            </Link>
          </div>
        </form>

        <div className={styles.divider}>
          <span className={styles.dividerText}>또는</span>
        </div>

        <div className={styles.socialBtns}>
          {/* 네이버 로그인 */}
          <button
            type="button"
            className={styles.socialBtn}
            onClick={() => handleSocialLogin("naver")}
          >
            <img src={naverLogo} alt="naver" className={styles.socialLogo} />
            네이버로 시작하기
          </button>

          {/* 구글 로그인 */}
          <button
            type="button"
            className={styles.socialBtn}
            onClick={() => handleSocialLogin("google")}
          >
            <img src={googleLogo} alt="google" className={styles.socialLogo} />
            구글로 시작하기
          </button>

          {/* 카카오 로그인 */}
          <button
            type="button"
            className={styles.socialBtn}
            onClick={() => handleSocialLogin("kakao")}
          >
            <img src={kakaoLogo} alt="kakao" className={styles.socialLogo} />
            카카오로 시작하기
          </button>

          {/* 깃허브 로그인 */}
          <button
            type="button"
            className={styles.socialBtn}
            onClick={() => handleSocialLogin("github")}
          >
            <img src={githubLogo} alt="github" className={styles.socialLogo} />
            깃허브로 시작하기
          </button>
        </div>
      </div>
    </div>
  );
};

export default Login;
