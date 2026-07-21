import { Navigate, useSearchParams } from "react-router-dom";
import useAuthStore from "../../utils/useAuthStore";
import { useEffect, useState } from "react";
import axios from "axios";

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

  useEffect(() => {
    if (!token) return;

    axios
      .get(`${import.meta.env.VITE_API_BASE_URL}/auth/socialInfo`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      })
      .then((res) => {
        login({
          userId: res.data.userId,
          memberId: res.data.loginId,
          memberName: res.data.nickname,
          memberEmail: res.data.email,
          memberAddr: res.data.userLocation,
          token,
        });
      })
      .catch((err) => {
        console.log(err);
      });
  }, [token, login]);

  if (!token) {
    return <Navigate to="/login" />;
  }

  return <Navigate to="/" />;
};

export default SocialLogin;
