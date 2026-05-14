import { Route, Routes } from "react-router-dom";
import App from "./App";
import Login from "./Login";
import SignUp from "./SignUp";
import SocialLogin from "./SocialLogin";

// 주소창의 URL을 보고 알맞은 페이지를 보여주는 컴포넌트
const AppRouter = () => {
  return (
    <div>
      <Routes>
        <Route path="/" element={<App />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/sociallogin" element={<SocialLogin />} />
      </Routes>
    </div>
  );
};

export default AppRouter;
