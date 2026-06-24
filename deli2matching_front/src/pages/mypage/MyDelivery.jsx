import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom"; // 카드 클릭 시 상세 페이지 이동용
import axiosInstance from "../../utils/axios";
import styles from "./MyDelivery.module.css";

// MyDelivery: 참여 중인 배달 모집 목록
const MyDelivery = () => {
  const [join, setJoin] = useState(null);

  // 참여 중인 배달 목록 조회
  useEffect(() => {
    axiosInstance
      .get("/delivery/myJoin")
      .then((res) => setJoin(res.data || null))
      .catch((err) => console.log(err));
  }, []);

  console.log(join);
  return (
    <div className={styles.page}>
      {join === null ? (
        "참여하고 있는 배달 모집이 없습니다."
      ) : (
        <MyDeliveryCard
          postId={join.postId}
          restaurantName={join.restaurantName}
          minutesUntilDeadline={join.minutesUntilDeadline}
          currentMembers={join.currentMembers}
          targetMembers={join.targetMembers}
        />
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
    <div className={styles.card} onClick={() => navigate(`/view/${postId}`)}>
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
