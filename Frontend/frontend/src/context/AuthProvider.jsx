import React, { createContext, useState, useEffect, useContext } from 'react';
import keycloak from '../services/keycloak';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [isInitialized, setIsInitialized] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const [roles, setRoles] = useState([]);

  useEffect(() => {
    keycloak.init({ onLoad: 'check-sso', checkLoginIframe: false })
      .then((authenticated) => {
        setIsAuthenticated(authenticated);
        if (authenticated) {
          setRoles(keycloak.realmAccess?.roles || []);
          keycloak.loadUserProfile().then((profile) => {
            setUser(profile);
          });
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
  // with previous code, but typically you'd just check hasRole('ADMIN')
  const computedRole = roles.includes('ADMIN') ? 'ADMIN' : (isAuthenticated ? 'CLIENT' : 'GUEST');

  if (!isInitialized) {
    return <div className="text-center p-5">Loading Authentication...</div>;
  }

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, roles, login, logout, hasRole, role: computedRole }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
