import React, { useState, useEffect } from 'react';
import { Shield, Ban, CheckCircle } from 'lucide-react';
import api from '../api/axios'; // 1. ІМПОРТУЄМО НАЛАШТОВАНИЙ AXIOS
import './AdminPanel.css';

const AdminPanel = ({ onShowSuccessToast, onShowErrorToast }) => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  // 2. ФУНКЦІЯ getAuthHeaders БІЛЬШЕ НЕ ПОТРІБНА, якщо у вашому axios налаштований interceptor.
  // Але про всяк випадок додамо передачу токена через константу, якщо interceptor немає.
  const authConfig = {
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token')}`,
    },
  };

  // Завантаження користувачів
  const fetchUsers = async () => {
    try {
      // Запит робимо відносно baseURL вашого Axios (наприклад, '/admin/users')
      const response = await api.get('/admin/users', authConfig);

      // В Axios результат лежить у полі .data і вже є готовим масивом/об'єктом
      setUsers(response.data);
    } catch (error) {
      // Гнучке отримання помилки від бекенду
      const backendMessage = error.response?.data?.message || error.message;
      onShowErrorToast(backendMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  // Зміна ролі
  const handleRoleChange = async (userId, newRole) => {
    try {
      // Тіло запиту передаємо другим аргументом { role: newRole }, а конфіг із токеном - третім
      await api.put(`/admin/users/${userId}/role`, { role: newRole }, authConfig);

      onShowSuccessToast('Role updated successfully');
      fetchUsers();
    } catch (error) {
      const backendMessage = error.response?.data?.message || error.message;
      onShowErrorToast(backendMessage);
    }
  };

  // Бан / Розбан
  const handleToggleBan = async (userId, isBanned) => {
    const endpoint = isBanned ? 'unban' : 'ban';
    try {
      // Для PUT запиту без тіла передаємо порожній об'єкт {} другим аргументом, а конфіг - третім
      await api.put(`/admin/users/${userId}/${endpoint}`, {}, authConfig);

      onShowSuccessToast(`User ${isBanned ? 'unbanned' : 'banned'} successfully`);
      fetchUsers();
    } catch (error) {
      const backendMessage = error.response?.data?.message || error.message;
      onShowErrorToast(backendMessage);
    }
  };

  if (loading) return <div className="admin-loading">Loading user management panel...</div>;

  return (
    <div className="admin-panel">
      <div className="admin-panel__header">
        <Shield size={32} className="admin-icon" />
        <h2>User Management Panel</h2>
      </div>

      <div className="admin-panel__table-wrapper">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Username</th>
              <th>Email</th>
              <th>Current Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id} className={user.isBanned || user.banned ? 'row-banned' : ''}>
                <td>{user.username || `${user.firstName} ${user.lastName}`}</td>
                <td>{user.email}</td>
                <td>
                  <select
                    value={user.role}
                    onChange={(e) => handleRoleChange(user.id, e.target.value)}
                    className="role-select"
                  >
                    <option value="CLIENT">Client</option>
                    <option value="MANAGER">Manager</option>
                    <option value="COURIER">Courier</option>
                    <option value="ADMIN">Admin</option>
                  </select>
                </td>
                <td>
                  <span
                    className={`status-badge ${user.isBanned || user.banned ? 'status-banned' : 'status-active'}`}
                  >
                    {user.isBanned || user.banned ? 'Banned' : 'Active'}
                  </span>
                </td>
                <td>
                  <button
                    onClick={() => handleToggleBan(user.id, user.isBanned || user.banned)}
                    className={`btn-action ${user.isBanned || user.banned ? 'btn-unban' : 'btn-ban'}`}
                    title={user.isBanned || user.banned ? 'Unban user' : 'Ban user'}
                  >
                    {user.isBanned || user.banned ? <CheckCircle size={18} /> : <Ban size={18} />}
                    {user.isBanned || user.banned ? ' Unban' : ' Ban'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AdminPanel;