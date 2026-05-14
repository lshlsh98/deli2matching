import { useState } from "react";
import axiosInstance from "./utils/axios";
import useAuthStore from "./utils/useAuthStore";
import { useNavigate } from "react-router-dom";

function App() {
  const navigate = useNavigate();

  return (
    <>
      <h1>홈</h1>
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
  );
}

export default App;
