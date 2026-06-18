import { Route, Routes } from "react-router-dom";
import App from "./App";
import Login from "./Login";
import SignUp from "./SignUp";
import SocialLogin from "./SocialLogin";

import Chatting from "./pages/chat/Chatting.jsx";
import Histories from "./pages/histories/Histories.jsx";
import HistoryDetail from "./pages/histories/HistoryDetail.jsx";
import Home from "./pages/home/Home.jsx";
import LoginHome from "./pages/home/LoginHome.jsx";
import LoginForm from "./pages/login/LoginForm.jsx";
import MatchingAddForm from "./pages/matchings/MatchingAddForm.jsx";
import MatchingDetail from "./pages/matchings/MatchingDetail.jsx";
import Matchings from "./pages/matchings/Matchings.jsx";
import MyMatching from "./pages/matchings/MyMatching.jsx";
import UserAddForm from "./pages/users/UserAddForm.jsx";
import UserLocation from "./pages/users/UserLocation.jsx";

// 주소창의 URL을 보고 알맞은 페이지를 보여주는 컴포넌트
const AppRouter = () => {
  return (
    <div>
      <Routes>
        <Route path="/" element={<App />} />
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/sociallogin" element={<SocialLogin />} />

        <Route path="/chatting" element={<Chatting />} />
        <Route path="/histories" element={<Histories />} />
        <Route path="/histories/detail" element={<HistoryDetail />} />
        <Route path="/home" element={<Home />} />
        <Route path="/home/login" element={<LoginHome />} />
        <Route path="/login/form" element={<LoginForm />} />
        <Route path="/matching/add" element={<MatchingAddForm />} />
        <Route path="/matching/detail" element={<MatchingDetail />} />
        <Route path="/matchings" element={<Matchings />} />
        <Route path="/mymatching" element={<MyMatching />} />
        <Route path="/useradd" element={<UserAddForm />} />
        <Route path="/userlocation" element={<UserLocation />} />
      </Routes>
    </div>
  );
};

export default AppRouter;
