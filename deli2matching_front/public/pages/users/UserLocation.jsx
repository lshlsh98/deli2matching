import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '../../utils/axios';
import styles from './UserLocation.module.css';

const NAVER_CLIENT_ID = 'CxCgbFIIikgQIFTL3Zhv';
const NAVER_CLIENT_SECRET = 'EVU9AHX4fk';
const CORS_PROXY = 'https://cors-anywhere.herokuapp.com/';

const UserLocation = () => {
  const navigate = useNavigate();
  const mapRef = useRef(null);
  const naverMapRef = useRef(null);
  const markerRef = useRef(null);
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [selectedAddress, setSelectedAddress] = useState('');
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    if (!window.naver) return;
    naverMapRef.current = new window.naver.maps.Map(mapRef.current, {
      center: new window.naver.maps.LatLng(37.5665, 126.9780),
      zoom: 15,
      mapTypeControl: true,
    });

    window.naver.maps.Event.addListener(naverMapRef.current, 'click', (e) => {
      const latlng = e.coord;
      window.naver.maps.Service.reverseGeocode({ coords: latlng, orders: 'roadaddr,addr' }, (status, response) => {
        if (status !== window.naver.maps.Service.Status.OK) return;
        const rs = response.v2.results;
        let roadAddr = '';
        rs.forEach((r) => { if (r.name === 'roadaddr') roadAddr = makeFullAddress(r); });
        updateSelection(latlng, roadAddr || '선택한 위치');
      });
    });
  }, []);

  const makeFullAddress = (item) => {
    const region = item.region || {};
    const land = item.land || {};
    const parts = [];
    if (region.area1?.name) parts.push(region.area1.name);
    if (region.area2?.name) parts.push(region.area2.name);
    if (region.area3?.name) parts.push(region.area3.name);
    if (region.area4?.name) parts.push(region.area4.name);
    if (land.name) parts.push(land.name);
    if (land.number1) parts.push(land.number2 ? `${land.number1}-${land.number2}` : land.number1);
    return parts.join(' ');
  };

  const updateSelection = (point, address) => {
    if (markerRef.current) markerRef.current.setMap(null);
    markerRef.current = new window.naver.maps.Marker({ map: naverMapRef.current, position: point });
    naverMapRef.current.setCenter(point);
    setSelectedAddress(address);
  };

  const searchLocal = async (q) => {
    setSearching(true);
    try {
      const url = `${CORS_PROXY}https://openapi.naver.com/v1/search/local.json?query=${encodeURIComponent(q)}&display=10&start=1`;
      const res = await fetch(url, {
        headers: { 'X-Naver-Client-Id': NAVER_CLIENT_ID, 'X-Naver-Client-Secret': NAVER_CLIENT_SECRET },
      });
      const data = await res.json();
      setResults(data.items ?? []);
      if (data.items?.length > 0) selectPOI(data.items[0]);
    } catch {
      setResults([]);
    } finally {
      setSearching(false);
    }
  };

  const selectPOI = (item) => {
    const lat = item.mapy / 10000000;
    const lng = item.mapx / 10000000;
    const point = new window.naver.maps.LatLng(lat, lng);
    updateSelection(point, item.roadAddress || item.address || item.title.replace(/<[^>]*>/g, ''));
  };

  const handleSearch = () => {
    if (!query) return;
    window.naver.maps.Service.geocode({ query }, (status, response) => {
      if (status === window.naver.maps.Service.Status.OK && response.v2.addresses.length > 0) {
        const addr = response.v2.addresses[0];
        const point = new window.naver.maps.LatLng(addr.y, addr.x);
        updateSelection(point, addr.roadAddress || addr.jibunAddress);
      } else {
        searchLocal(query);
      }
    });
  };

  const handleSend = async () => {
    if (!selectedAddress) { alert('먼저 주소를 선택해주세요.'); return; }
    try {
      await axiosInstance.post('/users/location', { address: selectedAddress });
      navigate('/matchings');
    } catch (err) {
      alert('서버 전송 실패: ' + (err.response?.status ?? ''));
    }
  };

  return (
    <div className={styles.mapWidgetWrap}>
      <div className={styles.searchPanel}>
        <h2>통합 검색</h2>
        <div className={styles.searchInputGroup}>
          <input
            type="text"
            className={styles.address}
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            placeholder="주소, 상호명, 장소명 검색"
          />
          <button type="button" className={styles.submitSearch} onClick={handleSearch}>검색</button>
        </div>
        <div id="search-results-list">
          {searching && <p>검색 중...</p>}
          {!searching && results.length === 0 && <p>검색어를 입력하고 검색 버튼을 눌러주세요.</p>}
          {!searching && results.length > 0 && (
            <ul className={styles.resultList}>
              {results.map((item, i) => (
                <li key={i} onClick={() => selectPOI(item)} className={styles.resultItem}>
                  <strong>{item.title.replace(/<[^>]*>/g, '')}</strong>
                  <small>{item.roadAddress || item.address}</small>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <div className={styles.mapContainer}>
        <div className={styles.mapArea}>
          <div ref={mapRef} className={styles.map} />
        </div>
        <div className={styles.selectedAddress}>
          <strong>선택된 주소:</strong>{' '}
          <span>{selectedAddress || '장소를 검색하거나 지도에서 선택하세요.'}</span>
        </div>
        <div className={styles.sendRow}>
          <button type="button" className={styles.sendAddress} onClick={handleSend}>주소 전송</button>
        </div>
      </div>
    </div>
  );
};

export default UserLocation;
