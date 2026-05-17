import http from 'k6/http';
import { check } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 100 },   // разгон
    { duration: '30s', target: 1000 },  // нагрузка 1000 пользователей
    { duration: '10s', target: 0 },     // спад
  ],
};

export default function () {
  const url = 'http://localhost:8080/api/v1/wallet';

  const payload = JSON.stringify({
    id: '11111111-1111-1111-1111-111111111111',
    operationType: 'DEPOSIT',
    balance: 1
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 200 or 409': (r) => r.status === 200 || r.status === 409,
  });
}