import React, { useState, useEffect } from 'react';
import { Shield, Ban, CheckCircle } from 'lucide-react';
import api from '../api/axios'; // 1. ІМПОРТУЄМО НАЛАШТОВАНИЙ AXIOS
import './AdminPanel.css';

const AdminPanel = ({ onShowSuccessToast, onShowErrorToast }) => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const currentAdminEmail = localStorage.getItem('userEmail');

  const authConfig = {
    headers: {
      Authorization: `Bearer ${localStorage.getItem('token')}`,
    },
  };

  const fetchUsers = async () => {
    try {
      const response = await api.get('/admin/users', authConfig);

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

  const handleRoleChange = async (userId, newRole) => {
    try {
      await api.put(`/admin/users/${userId}/role`, { role: newRole }, authConfig);

      onShowSuccessToast('Role updated successfully');
      fetchUsers();
    } catch (error) {
      const backendMessage = error.response?.data?.message || error.message;
      onShowErrorToast(backendMessage);
    }
  };

  const handleToggleBan = async (userId, isBanned) => {
    const endpoint = isBanned ? 'unban' : 'ban';
    try {
      await api.put(`/admin/users/${userId}/${endpoint}`, {}, authConfig);

      onShowSuccessToast(`User ${isBanned ? 'unbanned' : 'banned'} successfully`);
      fetchUsers();
    } catch (error) {
      let backendMessage = error.message;
      if (error.response && error.response.data) {
        if (typeof error.response.data === 'string') {
          backendMessage = error.response.data;
        } else if (error.response.data.message) {
          backendMessage = error.response.data.message;
        }
      }
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
                    disabled={user.email === currentAdminEmail}
                    className={`btn-action ${user.isBanned || user.banned ? 'btn-unban' : 'btn-ban'} ${user.email === currentAdminEmail ? 'btn-disabled' : ''}`}
                    title={
                      user.email === currentAdminEmail
                        ? 'You cannot ban yourself'
                        : user.isBanned || user.banned
                          ? 'Unban user'
                          : 'Ban user'
                    }
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
