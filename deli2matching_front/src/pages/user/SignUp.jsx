import { Link, useNavigate } from "react-router-dom";
import axiosInstance from "../../utils/axios";
import { useState } from "react";

// SignUp 컴포넌트: 회원가입 페이지 화면
const SignUp = () => {
  const navigate = useNavigate();

  const [member, setMember] = useState({
    loginId: "",
    password: "",
    username: "",
  });

  const inputMember = (e) => {
    const newMember = { ...member, [e.target.name]: e.target.value };
    setMember(newMember);
  };

  const joinMember = () => {
    // 서버에 회원가입 요청을 보냄
    // POST /auth/signup → 서버의 회원가입 처리 주소
    axiosInstance
      .post("/auth/signup", member)
      .then(() => {
        navigate("/login");
      })
      .catch((err) => {
        console.log(err);
      });
  };

  return (
    <section>
      <h3>회원가입</h3>
      <form
        onSubmit={(e) => {
          e.preventDefault();
          joinMember();
        }}
        autoComplete="off"
      >
        <div>
          <label htmlFor="memberId">아이디</label>
          <input
            type="text"
            name="loginId"
            id="loginId"
            value={member.loginId}
            onChange={inputMember}
          />
        </div>

        <div>
          <label htmlFor="password">비밀번호</label>
          <input
            type="password"
            name="password"
            id="password"
            value={member.password}
            onChange={inputMember}
          />
        </div>
        <div>
          <label htmlFor="username">이름</label>
          <input
            type="text"
            name="username"
            id="username"
            value={member.username}
            onChange={inputMember}
          />
        </div>
        <div>
          <button type="submit">회원가입</button>
        </div>
      </form>
    </section>
  );
};

// 다른 파일에서 SignUp을 쓸 수 있도록 내보내요.
export default SignUp;
