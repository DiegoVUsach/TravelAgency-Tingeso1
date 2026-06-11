import React, { useState, useEffect } from 'react';
import { Container, Table, Button, Spinner, Alert, Badge } from 'react-bootstrap';
import { userService } from '../services/userService';
import { useAuth } from '../context/AuthProvider';

function AdminUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const { role } = useAuth();

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = () => {
        setLoading(true);
        userService.getAllUsers()
            .then(data => {
                setUsers(data);
                setLoading(false);
            })
            .catch(err => {
                setError('Failed to fetch users.');
                setLoading(false);
            });
    };

    const handleToggleActive = (id) => {
        if (window.confirm('Are you sure you want to toggle the status of this user?')) {
            userService.toggleUserActive(id)
                .then(() => {
                    fetchUsers();
                })
                .catch(err => {
                    alert('Failed to update user status.');
                });
        }
    };

    if (role !== 'ADMIN') {
        return (
            <Container className="my-5 text-center py-5 glass-panel">
                <h3 className="mb-4 text-danger">Access Denied</h3>
                <p className="text-muted mb-4">You do not have permission to view the Users Dashboard.</p>
            </Container>
        );
    }

    return (
        <Container className="my-5 animate-fade-up">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div>
                    <h2>User Management</h2>
                    <p className="text-muted">Manage all registered users in the platform.</p>
                </div>
            </div>

            {loading && <div className="text-center my-5"><Spinner animation="border" variant="primary" /></div>}
            {error && <Alert variant="danger">{error}</Alert>}

            {!loading && !error && (
                <div className="glass-panel overflow-hidden">
                    <Table responsive hover className="mb-0 align-middle">
                        <thead className="bg-light">
                            <tr>
                                <th className="py-3 px-4 border-0">ID</th>
                                <th className="py-3 px-4 border-0">Name</th>
                                <th className="py-3 px-4 border-0">Email</th>
                                <th className="py-3 px-4 border-0">Phone</th>
                                <th className="py-3 px-4 border-0">Status</th>
                                <th className="py-3 px-4 border-0">Role</th>
                                <th className="py-3 px-4 border-0 text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map(user => (
                                <tr key={user.id}>
                                    <td className="px-4 text-muted">#{user.id}</td>
                                    <td className="px-4 fw-bold">{user.fullName || 'N/A'}</td>
                                    <td className="px-4">{user.email}</td>
                                    <td className="px-4">{user.phone || 'N/A'}</td>
                                    <td className="px-4">
                                        <Badge bg={user.active ? 'success' : 'danger'}>
                                            {user.active ? 'ACTIVE' : 'INACTIVE'}
                                        </Badge>
                                    </td>
                                    <td className="px-4">
                                        <Badge bg={user.role === 'ADMIN' ? 'primary' : 'secondary'}>
                                            {user.role}
                                        </Badge>
                                    </td>
                                    <td className="px-4 text-end">
                                        <Button
                                            variant={user.active ? "outline-danger" : "outline-success"}
                                            size="sm"
                                            onClick={() => handleToggleActive(user.id)}
                                        >
                                            {user.active ? 'Deactivate' : 'Activate'}
                                        </Button>
                                    </td>
                                </tr>
                            ))}
                            {users.length === 0 && (
                                <tr>
                                    <td colSpan="7" className="text-center py-5 text-muted">No users found.</td>
                                </tr>
                            )}
                        </tbody>
                    </Table>
                </div>
            )}
        </Container>
    );
}

export default AdminUsers;
