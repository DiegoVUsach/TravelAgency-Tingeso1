import React, { createContext, useState, useContext } from 'react';

const UserContext = createContext();

export const UserProvider = ({ children }) => {
  // Mock roles: 'GUEST', 'CLIENT', 'ADMIN'
  const [role, setRole] = useState('CLIENT');

  return (
    <UserContext.Provider value={{ role, setRole }}>
      {children}
    </UserContext.Provider>
  );
};

export const useUser = () => useContext(UserContext);

//check later and delete
