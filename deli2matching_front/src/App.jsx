import { useState } from "react";
import axiosInstance from "./utils/axios";
import useAuthStore from "./utils/useAuthStore";
import { useNavigate } from "react-router-dom";

function App() {
  const navigate = useNavigate();
  const token = useAuthStore((state) => {
    state.token;
  });

  const logout = useAuthStore((state) => {
    state.logout;
  });

  return (
    <>
      <h1>홈</h1>
      {token ? (
        <>
          <button
            onClick={() => {
              navigate("/login");
            }}
          >
            로그인
          </button>
          <button
            onClick={() => {
              navigate("/signup");
            }}
          >
            회원가입
          </button>
        </>
      ) : (
        <button
          onClick={() => {
            logout;
          }}
        >
          로그아웃
        </button>
      )}
    </>
  );
}

export default App;
