import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import {
  Activity,
  AtSign,
  Cake,
  Flame,
  HeartPulse,
  Ruler,
  Sparkles,
  Target,
  Users,
  KeyRound,
  Trash2,
} from 'lucide-react';
import './UserProfile.css';

const activityOptions = [
  { value: '1.2', label: 'Sedentary', hint: 'Little or no exercise' },
  { value: '1.38', label: 'Light', hint: 'Light exercise 1-3 days/week' },
  { value: '1.55', label: 'Moderate', hint: 'Moderate exercise 3-5 days/week' },
  { value: '1.73', label: 'Active', hint: 'Hard exercise 6-7 days/week' },
  { value: '1.9', label: 'Very Active', hint: 'Very hard training, physical job' },
];

const genderOptions = [
  { value: 'MALE', label: 'Male' },
  { value: 'FEMALE', label: 'Female' },
];

const targetOptions = [
  { value: 'LOSE', label: 'Lose' },
  { value: 'MAINTAIN', label: 'Maintain' },
  { value: 'GAIN', label: 'Gain' },
];

const emptyForm = {
  firstName: '',
  lastName: '',
  username: '',
  email: '',
  age: '',
  gender: '',
  height: '',
  weight: '',
  activityMultiplier: '',
  target: '',
  allergens: [],
  dailyCalorieIntake: 0,
};

const normalizeProfile = (data) => ({
  firstName: data?.firstName ?? '',
  lastName: data?.lastName ?? '',
  username: data?.username ?? '',
  email: data?.email ?? '',
  age: data?.age ?? '',
  gender: data?.gender ?? '',
  height: data?.height ?? '',
  weight: data?.weight ?? '',
  activityMultiplier:
    data?.activityMultiplier !== undefined && data?.activityMultiplier !== null
      ? String(data.activityMultiplier)
      : '',
  target: data?.target ?? '',
  allergens: Array.isArray(data?.allergens) ? data.allergens : [],
  dailyCalorieIntake: data?.dailyCalorieIntake ?? 0,
});

const UserProfile = ({ onShowSuccessToast, onShowErrorToast, onProfileUpdated = null }) => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState(emptyForm);
  const [availableAllergens, setAvailableAllergens] = useState([]);
  const [activeTab, setActiveTab] = useState('personal');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [passwordData, setPasswordData] = useState({ oldPassword: '', newPassword: '' });
  const [changingPassword, setChangingPassword] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleting, setDeleting] = useState(false);

  const userRole = localStorage.getItem('userRole') || '';

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem('token');

        const [profileResult, allergensResult] = await Promise.allSettled([
          api.get('/profile/me', {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }),
          api.get('/allergens'),
        ]);

        if (allergensResult.status === 'fulfilled') {
          const allergens = Array.isArray(allergensResult.value.data)
            ? allergensResult.value.data
            : [];
          setAvailableAllergens(allergens.filter((allergen) => allergen !== 'string'));
        } else {
          setAvailableAllergens([]);
        }

        if (profileResult.status === 'fulfilled') {
          setFormData((prev) => ({
            ...prev,
            ...normalizeProfile(profileResult.value.data),
          }));
          return;
        }

        const error = profileResult.reason;

        if (error?.response?.status === 404) {
          setFormData(emptyForm);
          return;
        }

        if (error?.response?.status === 401) {
          onShowErrorToast('Please log in to view your profile.');
          navigate('/login', { replace: true });
          return;
        }

        onShowErrorToast(error?.response?.data?.message || 'Could not load profile.');
      } catch (error) {
        onShowErrorToast(error?.response?.data?.message || 'Could not load profile.');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [navigate, onShowErrorToast]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const toggleAllergen = (allergen) => {
    setFormData((prev) => {
      const hasAllergen = prev.allergens.includes(allergen);
      return {
        ...prev,
        allergens: hasAllergen
          ? prev.allergens.filter((item) => item !== allergen)
          : [...prev.allergens, allergen],
      };
    });
  };

  const validate = () => {
    if (userRole === 'COURIER') return true;

    const age = Number(formData.age);
    const height = Number(formData.height);
    const weight = Number(formData.weight);

    if (!age || age <= 0 || !height || height <= 0 || !weight || weight <= 0) {
      onShowErrorToast('Please enter valid numbers');
      return false;
    }

    return true;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!validate()) {
      return;
    }

    try {
      setSaving(true);
      const token = localStorage.getItem('token');

      const payload = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        username: formData.username,
        age: Number(formData.age),
        gender: formData.gender,
        height: Number(formData.height),
        weight: Number(formData.weight),
        activityMultiplier: Number(formData.activityMultiplier),
        target: formData.target,
        allergens: formData.allergens,
      };

      const response = await api.post('/profile/update', payload, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const dailyCalorieIntake = response.data?.dailyCalories ?? response.data?.dailyCalorieIntake;

      const updatedProfile = await api.get('/profile/me', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      setFormData((prev) => ({
        ...prev,
        ...normalizeProfile(updatedProfile.data),
        dailyCalorieIntake:
          dailyCalorieIntake ?? updatedProfile.data?.dailyCalorieIntake ?? prev.dailyCalorieIntake,
      }));

      if (typeof onProfileUpdated === 'function') {
        try {
          await onProfileUpdated();
        } catch (refreshError) {
          console.error('Could not refresh nutrition summary after profile save', refreshError);
        }
      }

      onShowSuccessToast('Profile saved successfully!');
    } catch (error) {
      onShowErrorToast(error?.response?.data?.message || 'Could not save profile.');
    } finally {
      setSaving(false);
    }
  };

  const handleChangePasswordSubmit = async (e) => {
    e.preventDefault();
    if (passwordData.newPassword.length < 6) {
      onShowErrorToast('New password must be at least 6 characters long');
      return;
    }

    try {
      setChangingPassword(true);
      const token = localStorage.getItem('token');
      await api.post('/profile/change-password', passwordData, {
        headers: { Authorization: `Bearer ${token}` },
      });
      onShowSuccessToast('Password updated successfully!');
      setShowPasswordModal(false);
      setPasswordData({ oldPassword: '', newPassword: '' });
    } catch (error) {
      onShowErrorToast(
        error.response?.data?.message || error.response?.data || 'Failed to change password'
      );
    } finally {
      setChangingPassword(false);
    }
  };

  const handleDeleteAccountClick = () => {
    setShowDeleteModal(true);
  };
  const confirmDeleteAccount = async () => {
    try {
      setDeleting(true);
      const token = localStorage.getItem('token');
      await api.delete('/profile/delete', {
        headers: { Authorization: `Bearer ${token}` },
      });

      localStorage.clear();
      setShowDeleteModal(false);
      onShowSuccessToast('Your account has been deleted. Goodbye! 👋');
      navigate('/', { replace: true });
    } catch (error) {
      onShowErrorToast(error.response?.data?.message || 'Could not delete account.');
    } finally {
      setDeleting(false);
    }
  };

  if (loading) {
    return (
      <div className="profile-page profile-page--loading">
        <div className="profile-loading-card">Loading profile...</div>
      </div>
    );
  }

  const showSummaryCard =
    userRole !== 'COURIER' || formData.firstName || formData.lastName || formData.username;

  return (
    <div className="profile-page">
      <div className="profile-page__header">
        <h1>User Profile</h1>
        <p>Manage your profile details and settings.</p>
      </div>

      <div className="profile-layout">
        <section className="profile-card profile-form-card">
          {userRole !== 'COURIER' && (
            <div className="profile-tabs" role="tablist" aria-label="Profile sections">
              <button
                type="button"
                role="tab"
                aria-selected={activeTab === 'personal'}
                className={`profile-tab ${activeTab === 'personal' ? 'active' : ''}`}
                onClick={() => setActiveTab('personal')}
              >
                Personal Details
              </button>

              <button
                type="button"
                role="tab"
                aria-selected={activeTab === 'health'}
                className={`profile-tab ${activeTab === 'health' ? 'active' : ''}`}
                onClick={() => setActiveTab('health')}
              >
                Health Profile
              </button>
            </div>
          )}

          <form className="profile-form" onSubmit={handleSubmit}>
            {activeTab === 'personal' && (
              <div className="profile-form-section">
                <div className="profile-grid profile-grid--two profile-personal-grid">
                  <label className="profile-field">
                    <span>
                      <Users size={16} /> First Name
                    </span>
                    <input
                      type="text"
                      name="firstName"
                      value={formData.firstName}
                      onChange={handleChange}
                      placeholder="John"
                    />
                  </label>

                  <label className="profile-field">
                    <span>
                      <Users size={16} /> Last Name
                    </span>
                    <input
                      type="text"
                      name="lastName"
                      value={formData.lastName}
                      onChange={handleChange}
                      placeholder="Doe"
                    />
                  </label>
                </div>

                <div className="profile-grid profile-personal-grid">
                  <label className="profile-field">
                    <span>
                      <AtSign size={16} /> Username
                    </span>
                    <input
                      type="text"
                      name="username"
                      value={formData.username}
                      onChange={handleChange}
                      placeholder="john_doe"
                    />
                  </label>
                  <label className="profile-field">
                    <span>
                      <AtSign size={16} /> Email
                    </span>
                    <input type="email" name="email" value={formData.email} disabled />
                  </label>
                </div>

                <div className="profile-account-actions">
                  <h3>Account Settings</h3>
                  <div className="account-actions-btns">
                    <button
                      type="button"
                      className="profile-action-btn password-btn"
                      onClick={() => setShowPasswordModal(true)}
                    >
                      <KeyRound size={16} /> Change Password
                    </button>
                    <button
                      type="button"
                      className="profile-action-btn delete-btn"
                      onClick={handleDeleteAccountClick}
                    >
                      <Trash2 size={16} /> Delete Account
                    </button>
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'health' && userRole !== 'COURIER' && (
              <div className="profile-form-section">
                <div className="profile-grid profile-grid--two">
                  <label className="profile-field">
                    <span>
                      <Cake size={16} /> Age
                    </span>
                    <input
                      type="number"
                      name="age"
                      min="1"
                      value={formData.age}
                      onChange={handleChange}
                      placeholder="28"
                    />
                  </label>
                  <label className="profile-field">
                    <span>
                      <Users size={16} /> Gender
                    </span>
                    <select name="gender" value={formData.gender} onChange={handleChange}>
                      <option value="">Select gender</option>
                      {genderOptions.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>
                <div className="profile-grid profile-grid--two">
                  <label className="profile-field">
                    <span>
                      <Ruler size={16} /> Height (cm)
                    </span>
                    <input
                      type="number"
                      name="height"
                      min="1"
                      value={formData.height}
                      onChange={handleChange}
                      placeholder="175"
                    />
                  </label>
                  <label className="profile-field">
                    <span>
                      <HeartPulse size={16} /> Weight (kg)
                    </span>
                    <input
                      type="number"
                      name="weight"
                      min="1"
                      step="0.1"
                      value={formData.weight}
                      onChange={handleChange}
                      placeholder="72"
                    />
                  </label>
                </div>
                <label className="profile-field">
                  <span>
                    <Activity size={16} /> Activity multiplier
                  </span>
                  <select
                    name="activityMultiplier"
                    value={formData.activityMultiplier}
                    onChange={handleChange}
                  >
                    <option value="">Select activity level</option>
                    {activityOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label} ({option.value})
                      </option>
                    ))}
                  </select>
                </label>
                <label className="profile-field">
                  <span>
                    <Target size={16} /> Goal
                  </span>
                  <select name="target" value={formData.target} onChange={handleChange}>
                    <option value="">Select goal</option>
                    {targetOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </select>
                </label>
                <div className="profile-allergens">
                  <div className="profile-allergens__header">
                    <Sparkles size={18} />
                    <h3>Allergens</h3>
                  </div>
                  <div className="profile-allergens__grid">
                    {availableAllergens
                      .filter((a) => a !== 'string')
                      .map((allergen) => {
                        const active = formData.allergens.includes(allergen);
                        return (
                          <button
                            key={allergen}
                            type="button"
                            className={`allergen-pill ${active ? 'active' : ''}`}
                            onClick={() => toggleAllergen(allergen)}
                          >
                            <span className="allergen-pill__dot" />
                            {allergen.charAt(0).toUpperCase() + allergen.slice(1)}
                          </button>
                        );
                      })}
                  </div>
                </div>
              </div>
            )}

            <button type="submit" className="profile-save-btn" disabled={saving}>
              {saving ? 'Saving...' : 'Save Profile'}
            </button>
          </form>
        </section>

        {showSummaryCard && (
          <aside className="profile-card profile-summary-card">
            {(formData.firstName || formData.lastName || formData.username) && (
              <div className="profile-summary__identity">
                <strong>
                  {[formData.firstName, formData.lastName].filter(Boolean).join(' ') || 'User Name'}
                </strong>
                <span>{formData.username ? `@${formData.username}` : 'User Name'}</span>
              </div>
            )}
            {userRole !== 'COURIER' && (
              <>
                <div className="profile-card__title">
                  <Flame size={20} />
                  <h2>Daily Goal</h2>
                </div>
                <div className="profile-summary__goal">
                  {Number(formData.dailyCalorieIntake) > 0 ? (
                    <strong>
                      Your Daily Goal: {Math.round(Number(formData.dailyCalorieIntake))} kcal
                    </strong>
                  ) : (
                    <strong>Fill the form to calculate your daily goal</strong>
                  )}
                </div>
                <div className="profile-summary__list">
                  <div className="profile-summary__row">
                    <span>Age</span>
                    <strong>{formData.age || '-'}</strong>
                  </div>
                  <div className="profile-summary__row">
                    <span>Gender</span>
                    <strong>{formData.gender || '-'}</strong>
                  </div>
                  <div className="profile-summary__row">
                    <span>Height</span>
                    <strong>{formData.height ? `${formData.height} cm` : '-'}</strong>
                  </div>
                  <div className="profile-summary__row">
                    <span>Weight</span>
                    <strong>{formData.weight ? `${formData.weight} kg` : '-'}</strong>
                  </div>
                  <div className="profile-summary__row">
                    <span>Activity</span>
                    <strong>{formData.activityMultiplier || '-'}</strong>
                  </div>
                  <div className="profile-summary__row">
                    <span>Target</span>
                    <strong>{formData.target || '-'}</strong>
                  </div>
                </div>
              </>
            )}
          </aside>
        )}
      </div>
      {showPasswordModal && (
        <div className="profile-modal-overlay" onClick={() => setShowPasswordModal(false)}>
          <div className="profile-modal" onClick={(e) => e.stopPropagation()}>
            <h2>Change Password</h2>
            <form onSubmit={handleChangePasswordSubmit}>
              <label className="profile-field">
                <span>Old Password</span>
                <input
                  type="password"
                  required
                  value={passwordData.oldPassword}
                  onChange={(e) =>
                    setPasswordData({ ...passwordData, oldPassword: e.target.value })
                  }
                  placeholder="Enter old password"
                />
              </label>
              <label className="profile-field">
                <span>New Password</span>
                <input
                  type="password"
                  required
                  value={passwordData.newPassword}
                  onChange={(e) =>
                    setPasswordData({ ...passwordData, newPassword: e.target.value })
                  }
                  placeholder="Minimum 6 characters"
                />
              </label>
              <div className="modal-buttons">
                <button
                  type="button"
                  className="modal-btn modal-btn--cancel"
                  onClick={() => {
                    setShowPasswordModal(false);
                    setPasswordData({ oldPassword: '', newPassword: '' });
                  }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="modal-btn modal-btn--submit"
                  disabled={changingPassword}
                >
                  {changingPassword ? 'Updating...' : 'Update Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
      {showDeleteModal && (
        <div className="profile-modal-overlay" onClick={() => setShowDeleteModal(false)}>
          <div className="profile-modal" onClick={(e) => e.stopPropagation()}>
            <h2>Are you sure you want to delete your account?</h2>
            <p className="profile-modal-warning-text">This action cannot be undone.</p>
            <div className="modal-buttons">
              <button
                type="button"
                className="modal-btn modal-btn--cancel"
                onClick={() => setShowDeleteModal(false)}
                disabled={deleting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="modal-btn modal-btn--submit delete-confirm-btn"
                onClick={confirmDeleteAccount}
                disabled={deleting}
              >
                {deleting ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserProfile;
