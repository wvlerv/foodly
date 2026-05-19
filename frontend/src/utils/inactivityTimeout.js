import authService from '../services/authService';

const TWENTY_FOUR_HOURS = 100 * 1000;
let inactivityTimer = null;

const performAutoLogout = async () => {
  console.warn('User inactive. Triggering logout...');
  await authService.logout();

  window.location.replace('/login?reason=inactivity');
};

export const resetInactivityTimer = () => {
  if (inactivityTimer) clearTimeout(inactivityTimer);
  if (!localStorage.getItem('token')) return;
  inactivityTimer = setTimeout(performAutoLogout, TWENTY_FOUR_HOURS);
};

export const initInactivityTracking = () => {
  const activityEvents = ['mousedown', 'mousemove', 'keydown', 'scroll', 'touchstart', 'click'];
  resetInactivityTimer();
  activityEvents.forEach((event) => {
    window.addEventListener(event, resetInactivityTimer);
  });
};
