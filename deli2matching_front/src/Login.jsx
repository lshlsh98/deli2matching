import { Link, useNavigate } from "react-router-dom";
import axiosInstance from "./utils/axios";
import useAuthStore from "./utils/useAuthStore";

// Login 컴포넌트: 로그인 페이지 화면
const Login = () => {
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);

  // handleSubmit: 로그인 버튼을 눌렀을 때 실행되는 함수
  const handleSubmit = (event) => {
    // 기본 동작(페이지 새로고침)을 막음
    event.preventDefault();

    // FormData로 폼 안에 입력된 값들을 가져와요.
    const data = new FormData(event.target);
    const username = data.get("username"); // 아이디 입력값
    const password = data.get("password"); // 비밀번호 입력값

    axiosInstance
      .post("/auth/signin", { username, password })
      .then((response) => {
        if (response.data.token) {
          login({ token: response.data.token });
          navigate("/");
        }
      })
      .catch((err) => {
        console.log(err);
      });
  };

  // handleSocialLogin: 소셜 로그인 버튼을 눌렀을 때 실행되는 함수
  // provider = 어떤 소셜 서비스인지 (예: "google", "naver", "kakao", "github")
  const handleSocialLogin = (provider) => {
    // 현재 프론트엔드 주소를 가져옴
    // 예) http://localhost:5173
    const frontendUrl = window.location.protocol + "//" + window.location.host;
    const API_BASE_URL = "http://localhost:8080";

    // 소셜 로그인 페이지로 이동
    // 서버가 소셜 로그인을 처리한 뒤 frontendUrl로 돌아옴
    window.location.href =
      API_BASE_URL +
      "/oauth2/authorization/" +
      provider +
      "?redirect_url=" +
      frontendUrl;
  };

  return (
    <form noValidate onSubmit={handleSubmit}>
      {/* 아이디 입력란 */}

      {/* 비밀번호 입력란 */}

      {/* 로그인 버튼 (type="submit"이면 폼의 onSubmit이 실행돼요) */}
      <button type="submit" fullWidth variant="contained" color="primary">
        로그인
      </button>
      {/* 소셜 로그인 버튼들 */}
      <button
        onClick={() => handleSocialLogin("google")}
        fullWidth
        variant="contained"
      >
        구글로 로그인하기
      </button>
      <button
        onClick={() => handleSocialLogin("naver")}
        fullWidth
        variant="contained"
      >
        네이버로 로그인하기
      </button>
      <button
        onClick={() => handleSocialLogin("kakao")}
        fullWidth
        variant="contained"
      >
        카카오로 로그인하기
      </button>
      <button
        onClick={() => handleSocialLogin("github")}
        fullWidth
        variant="contained"
      >
        깃허브로 로그인하기
      </button>
      {/* 회원가입 페이지로 가는 링크 */}
      <Link to="/signup" variant="body2">
        계정이 없습니까? 여기서 가입 하세요.
      </Link>
    </form>
  );
};

// 다른 파일에서 Login을 쓸 수 있도록 내보내요.
export default Login;
