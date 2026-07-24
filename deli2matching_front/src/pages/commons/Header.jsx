import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import useAuthStore from "../../utils/useAuthStore";
import styles from "./Header.module.css";
import logo from "../../assets/logo/gachi_logo.png";
import axiosInstance from "../../utils/axios";

function Header() {
  const token = useAuthStore((state) => state.token);
  const admin = useAuthStore((state) => state.admin);
  const logout = useAuthStore((state) => state.logout);
  const location = useLocation();
  const [roomId, setRoomId] = useState(null);

  useEffect(() => {
    if (!token) return;

    axiosInstance.get("/chat/room/group/list").then((res) => {
      setRoomId(res.data === "" ? null : res.data);
    });
  }, [location.pathname]);

  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <div className={styles.logoArea}>
          <div className={styles.logo}>
            <Link to="/">
              <img src={logo} alt="같이시켜 로고" className={styles.logoImg} />
            </Link>
          </div>
        </div>

        {token && roomId !== null && (
          <nav className={styles.centerMenu}>
            <button
              className={
                location.pathname.startsWith("/mychat") ? styles.activeMenu : ""
              }
            >
              <Link to={`/mychat/${roomId}`}>채팅</Link>
            </button>
          </nav>
        )}

        <div className={styles.rightArea}>
          {token ? (
            <div className={styles.userMenu}>
              <Link to="/mypage/myinfo">
                <button aria-label="마이 페이지" className={styles.iconBtn}>
                  My Page
                </button>
              </Link>

              <Link to={"/"}>
                <button
                  aria-label="로그아웃"
                  className={styles.iconBtn}
                  onClick={logout}
                >
                  Logout
                </button>
              </Link>
            </div>
          ) : (
            <div className={styles.authMenu}>
              <button>
                <Link to="/login">Login</Link>
              </button>

              <button>
                <Link to="/signup">Sign Up</Link>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;
