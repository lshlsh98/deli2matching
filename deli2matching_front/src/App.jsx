import { Route, Routes } from "react-router-dom";
import Home from "./pages/commons/Header";
import Login from "./pages/user/Login";
import SignUp from "./pages/user/SignUp";
import SocialLogin from "./pages/user/SocialLogin";
import Header from "./pages/commons/Header";
import "./App.css";

function App() {
  return (
    <div className="wrap">
      <Header />
      <div className="main">
        <Routes>
          {/* <Route path="/" element={<Home />} /> */}
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/sociallogin" element={<SocialLogin />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
