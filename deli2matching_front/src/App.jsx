import { Route, Routes } from "react-router-dom";
import Login from "./pages/user/Login";
import SignUp from "./pages/user/SignUp";
import SocialLogin from "./pages/user/SocialLogin";
import Header from "./pages/commons/Header";
import "./App.css";
import DeliveryList from "./pages/delivery/DeliveryList";
import DeliveryRegist from "./pages/delivery/DeliveryRegist";

function App() {
  return (
    <div className="wrap">
      <Header />
      <div className="main">
        <Routes>
          <Route path="/" element={<DeliveryList />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<SignUp />} />
          <Route path="/sociallogin" element={<SocialLogin />} />
          <Route path="/regist" element={<DeliveryRegist />} />
        </Routes>
      </div>
    </div>
  );
}

export default App;
