import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom"; // 카드 클릭 시 상세 페이지 이동용
import axiosInstance from "../../utils/axios";
import styles from "./MyDelivery.module.css";

// MyDelivery: 참여 중인 배달 모집 목록
const MyDelivery = () => {
  const [list, setList] = useState([]);

  // 참여 중인 배달 목록 조회
  useEffect(() => {
    axiosInstance
      .get("/member/deliveries")
      .then((res) => setList(res.data))
      .catch((err) => console.log(err));
  }, []);

  return (
    <div className={styles.page}>
      {list.length > 0 ? (
        <div className={styles.card_list}>
          {list.map((item) => (
            <MyDeliveryCard
              key={item.postId}
              postId={item.postId}
              restaurantName={item.restaurantName}
              minutesUntilDeadline={item.minutesUntilDeadline}
              currentMembers={item.currentMembers}
              targetMembers={item.targetMembers}
            />
          ))}
        </div>
      ) : (
        <p className={styles.empty}>참여 중인 모집이 없습니다.</p>
      )}
    </div>
  );
};

const MyDeliveryCard = ({
  postId,
  restaurantName,
  minutesUntilDeadline,
  currentMembers,
  targetMembers,
}) => {
  const navigate = useNavigate();

  const progressPercent = Math.round((currentMembers / targetMembers) * 100);

  return (
    <div
      className={styles.card}
      onClick={() => navigate(`/delivery/${postId}`)}
    >
      {/* 식당 이름 */}
      <h3 className={styles.card_name}>{restaurantName}</h3>

      {/* 남은 시간 */}
      <div className={styles.card_meta}>
        <span className={styles.card_clock}>&#x23F1;</span>
        <span className={styles.card_time}>
          남은 시간: {minutesUntilDeadline}분
        </span>
      </div>

      {/* 참여 현황 + 진행률 바 */}
      <div className={styles.card_bottom}>
        <span className={styles.card_count}>
          {currentMembers}/{targetMembers}명 모집 중
        </span>
        <div className={styles.card_progress_track}>
          <div
            className={styles.card_progress_fill}
            style={{ width: `${progressPercent}%` }}
          />
        </div>
      </div>
    </div>
  );
};

export default MyDelivery;
