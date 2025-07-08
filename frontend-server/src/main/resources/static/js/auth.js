/**
 * Authentication utilities for interacting with User Service APIs
 */

const API_BASE_URL = '/api/auth'; // This should be routed through your gateway to user-service

// Login with email and password
async function login(email, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Login failed');
        }

        const data = await response.json();
        // Store auth tokens
        storeAuthData(data);
        return data;
    } catch (error) {
        console.error('Login error:', error);
        throw error;
    }
}

// Register new user
async function register(userData) {
    try {
        const response = await fetch(`${API_BASE_URL}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Registration failed');
        }

        return await response.json();
    } catch (error) {
        console.error('Registration error:', error);
        throw error;
    }
}

// Logout user and clear tokens
async function logout() {
    try {
        // Get the current refresh token
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
            return;
        }

        // Attempt to revoke the token on server
        await fetch(`${API_BASE_URL}/logout`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({ refreshToken })
        }).catch(err => console.warn('Server logout failed:', err));
    } finally {
        // Clear local storage regardless of server response
        clearAuthData();
    }
}

// Refresh the access token using refresh token
async function refreshToken() {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
        clearAuthData();
        throw new Error('No refresh token available');
    }

    try {
        const response = await fetch(`${API_BASE_URL}/refresh-token`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ refreshToken })
        });

        if (!response.ok) {
            clearAuthData();
            throw new Error('Token refresh failed');
        }

        const data = await response.json();
        storeAuthData(data);
        return data.token;
    } catch (error) {
        clearAuthData();
        throw error;
    }
}

// Get user profile
async function getUserProfile() {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/me`);
        if (!response.ok) {
            throw new Error('Failed to fetch user profile');
        }
        return await response.json();
    } catch (error) {
        console.error('Error fetching profile:', error);
        throw error;
    }
}

// Get active sessions
async function getActiveSessions() {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/sessions`);
        if (!response.ok) {
            throw new Error('Failed to fetch sessions');
        }
        return await response.json();
    } catch (error) {
        console.error('Error fetching sessions:', error);
        throw error;
    }
}

// Terminate a session
async function terminateSession(sessionId) {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/sessions/${sessionId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Failed to terminate session');
        }

        return true;
    } catch (error) {
        console.error('Error terminating session:', error);
        throw error;
    }
}

// Update user profile
async function updateUserProfile(userData) {
    try {
        const response = await fetchWithAuth(`${API_BASE_URL}/update-profile`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            throw new Error('Failed to update profile');
        }

        return await response.json();
    } catch (error) {
        console.error('Error updating profile:', error);
        throw error;
    }
}

// Helper function to make authenticated requests
async function fetchWithAuth(url, options = {}) {
    if (!isAuthenticated()) {
        throw new Error('User not authenticated');
    }

    // Clone the options to avoid modifying the original
    const authOptions = { ...options };

    // Add Authorization header
    authOptions.headers = {
        ...authOptions.headers,
        'Authorization': `Bearer ${localStorage.getItem('token')}`
    };

    let response = await fetch(url, authOptions);

    // If unauthorized, try to refresh token
    if (response.status === 401) {
        try {
            // Attempt to refresh the token
            const newToken = await refreshToken();

            // Update Authorization header with new token
            authOptions.headers['Authorization'] = `Bearer ${newToken}`;

            // Retry the request
            response = await fetch(url, authOptions);
        } catch (error) {
            clearAuthData();
            throw new Error('Session expired. Please login again.');
        }
    }

    return response;
}

// Check if user is authenticated
function isAuthenticated() {
    const token = localStorage.getItem('token');
    if (!token) {
        return false;
    }

    // Check token expiration
    try {
        const payload = parseJwt(token);
        return Date.now() < payload.exp * 1000; // Convert exp to milliseconds
    } catch (e) {
        return false;
    }
}

// Parse JWT token
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error('Error parsing JWT:', e);
        return null;
    }
}

// Get user information from token
function getUserInfo() {
    if (!isAuthenticated()) {
        return null;
    }

    const token = localStorage.getItem('token');
    return parseJwt(token);
}

// Store authentication data
function storeAuthData(data) {
    localStorage.setItem('token', data.token);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('userEmail', data.email);
    localStorage.setItem('authTime', Date.now().toString());
}

// Clear authentication data
function clearAuthData() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('authTime');
}

// Export functions for use in other modules
window.Auth = {
    login,
    register,
    logout,
    getUserProfile,
    getActiveSessions,
    terminateSession,
    updateUserProfile,
    isAuthenticated,
    getUserInfo
};