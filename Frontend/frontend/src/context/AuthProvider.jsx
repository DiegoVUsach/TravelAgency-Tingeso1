import React, { createContext, useState, useEffect, useContext, useRef, useCallback } from 'react';
import keycloak from '../services/keycloak';
import { userService } from '../services/userService';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isInitialized, setIsInitialized] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null); // Keycloak user
  const [localUser, setLocalUser] = useState(null); // Backend DB user profile
  const [roles, setRoles] = useState([]);

  const isRun = useRef(false);

  const refreshProfile = useCallback(async () => {
    if (isAuthenticated) {
      try {
        const profile = await userService.getMyProfile();
        setLocalUser(profile);
      } catch (err) {
        console.error("Failed to fetch local user profile:", err);
      }
    }
  }, [isAuthenticated]);

  useEffect(() => {
    if (isRun.current) return;
    isRun.current = true;

    keycloak.init({ onLoad: 'check-sso', checkLoginIframe: false })
      .then(async (authenticated) => {
        setIsAuthenticated(authenticated);
        if (authenticated) {
          setRoles(keycloak.realmAccess?.roles || []);
          try {
            const profile = await keycloak.loadUserProfile();
            setUser(profile);
            
            // Sync user with our backend
            const syncedUser = await userService.syncUser();
            setLocalUser(syncedUser);
          } catch (err) {
             console.error("Error loading profile or syncing user", err);
          }
        }
        setIsInitialized(true);
      })
      .catch((error) => {
        console.error("Keycloak init failed:", error);
        setIsInitialized(true);
      });
  }, []);

  const login = () => keycloak.login();
  const logout = () => keycloak.logout();
  const hasRole = (role) => roles.includes(role);

  // We expose "role" as a computed property for easier mock compatibility 
  const computedRole = roles.includes('ADMIN') ? 'ADMIN' : (isAuthenticated ? 'CLIENT' : 'GUEST');

  if (!isInitialized) {
    return <div className="text-center p-5">Loading Authentication...</div>;
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, localUser, roles, login, logout, hasRole, role: computedRole, refreshProfile }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);