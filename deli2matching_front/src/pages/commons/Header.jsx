import { useState } from "react";
import { Link, useLocation } from "react-router-dom";
import useAuthStore from "../../utils/useAuthStore";
import styles from "./Header.module.css";
import logo from "../../assets/logo/black_only.png";
import { FiBell, FiMail, FiUser, FiSettings } from "react-icons/fi";

function Header() {
  const token = useAuthStore((state) => state.token);
  const admin = useAuthStore((state) => state.admin);
  const logout = useAuthStore((state) => state.logout);

  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <div className={styles.logoArea}>
          <div className={styles.logo}>
            <Link to="/">
              <img src={logo} alt="같이 먹자 로고" className={styles.logoImg} />
              <div className={styles.logoText}>같이 먹자</div>
            </Link>
          </div>
        </div>

        {token && (
          <nav className={styles.centerMenu}>
            <button
              className={
                location.pathname.startsWith("/rest") ? styles.activeMenu : ""
              }
            >
              <Link to="#">#</Link>
            </button>

            <button
              className={
                location.pathname.startsWith("/trip") ? styles.activeMenu : ""
              }
            >
              <Link to="#">#</Link>
            </button>

            <button
              className={
                location.pathname.startsWith("/board") ? styles.activeMenu : ""
              }
            >
              <Link to="#">#</Link>
            </button>
          </nav>
        )}

        <div className={styles.rightArea}>
          {token ? (
            <div className={styles.userMenu}>
              <button aria-label="알림" className={styles.iconBtn}>
                <FiBell />
              </button>

              <button aria-label="메시지" className={styles.iconBtn}>
                <FiMail />
              </button>

              {admin ? (
                <Link to="/admin">
                  <button aria-label="관리자페이지" className={styles.iconBtn}>
                    <FiUser />
                  </button>
                </Link>
              ) : (
                <Link to="#">
                  <button aria-label="마이페이지" className={styles.iconBtn}>
                    <FiUser />
                  </button>
                </Link>
              )}

              <Link to={"/"}>
                <button
                  aria-label="설정"
                  className={styles.iconBtn}
                  onClick={logout}
                >
                  <FiSettings />
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
