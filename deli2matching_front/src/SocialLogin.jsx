import { Navigate, useSearchParams } from "react-router-dom";
import useAuthStore from "./utils/useAuthStore";

// SocialLogin 컴포넌트: 소셜 로그인(구글, 네이버 등)이 끝난 뒤 돌아오는 페이지
// 서버가 로그인을 처리한 뒤 "?token=xxx" 형태로 토큰을 URL에 담아 보내줌
// 예) http://localhost:5173/sociallogin?token=eyJhbGci...
const SocialLogin = () => {
  // useSearchParams로 URL의 쿼리 파라미터를 읽어요.
  // 예) /sociallogin?token=abc123 → searchParams.get("token") = "abc123"
  const [searchParams] = useSearchParams();

  const login = useAuthStore((state) => state.login);

  // URL에서 token 값을 꺼냄
  const token = searchParams.get("token");

  if (token) {
    // 토큰이 있으면 서랍장에 토큰을 저장하고 메인 페이지로 이동해요.
    login({ token }); // 서랍장에 토큰 저장
    return <Navigate to="/" />; // 메인 페이지(/)로 이동
  } else {
    // 토큰이 없으면 로그인 페이지로 돌려보냄
    return <Navigate to="/login" />;
  }
};

export default SocialLogin;
