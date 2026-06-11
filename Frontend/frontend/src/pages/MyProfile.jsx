import React, { useState, useEffect } from 'react';
import { Container, Form, Button, Alert, Spinner, Card } from 'react-bootstrap';
import { useAuth } from '../context/AuthProvider';
import { userService } from '../services/userService';
import { useNavigate } from 'react-router-dom';

function MyProfile() {
    const { localUser, refreshProfile, logout } = useAuth();
    const navigate = useNavigate();
    
    const [formData, setFormData] = useState({
        fullName: '',
        phone: '',
        identityDocument: '',
        nationality: ''
    });
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState({ type: '', text: '' });

    useEffect(() => {
        if (localUser) {
            setFormData({
                fullName: localUser.fullName || '',
                phone: localUser.phone || '',
                identityDocument: localUser.identityDocument || '',
                nationality: localUser.nationality || ''
            });
        }
    }, [localUser]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setMessage({ type: '', text: '' });

        try {
            await userService.updateMyProfile(formData);
            await refreshProfile();
            setMessage({ type: 'success', text: 'Profile updated successfully!' });
        } catch (error) {
            setMessage({ type: 'danger', text: 'Failed to update profile.' });
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteAccount = async () => {
        if (window.confirm("Are you sure you want to deactivate your account? This action cannot be fully undone if you have active reservations.")) {
            try {
                await userService.deleteMyAccount();
                alert("Account deactivated successfully. You will now be logged out.");
                logout();
                navigate('/');
            } catch (error) {
                setMessage({ type: 'danger', text: 'Failed to deactivate account.' });
            }
        }
    };

    if (!localUser) {
        return <Container className="text-center my-5 py-5"><Spinner animation="border" /></Container>;
    }

    return (
        <Container className="my-5 max-w-md animate-fade-up" style={{ maxWidth: '600px' }}>
            <Card className="glass-panel p-4 shadow-sm border-0">
                <Card.Body>
                    <h2 className="mb-4 text-center">My Profile</h2>
                    
                    {message.text && <Alert variant={message.type}>{message.text}</Alert>}

                    <Form onSubmit={handleSubmit}>
                        <Form.Group className="mb-3">
                            <Form.Label className="text-muted small fw-bold">Email (read-only)</Form.Label>
                            <Form.Control type="email" value={localUser.email} disabled className="bg-light" />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Full Name</Form.Label>
                            <Form.Control 
                                name="fullName" 
                                value={formData.fullName} 
                                onChange={handleInputChange} 
                                className="premium-input"
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Phone</Form.Label>
                            <Form.Control 
                                name="phone" 
                                value={formData.phone} 
                                onChange={handleInputChange}
                                className="premium-input" 
                            />
                        </Form.Group>

                        <Form.Group className="mb-3">
                            <Form.Label className="fw-bold">Identity Document</Form.Label>
                            <Form.Control 
                                name="identityDocument" 
                                value={formData.identityDocument} 
                                onChange={handleInputChange}
                                className="premium-input" 
                            />
                        </Form.Group>

                        <Form.Group className="mb-4">
                            <Form.Label className="fw-bold">Nationality</Form.Label>
                            <Form.Control 
                                name="nationality" 
                                value={formData.nationality} 
                                onChange={handleInputChange}
                                className="premium-input" 
                            />
                        </Form.Group>

                        <div className="d-grid gap-2">
                            <Button type="submit" variant="primary" className="btn-premium" disabled={loading}>
                                {loading ? <Spinner size="sm" /> : 'Update Profile'}
                            </Button>
                        </div>
                    </Form>

                    <hr className="my-4" />
                    
                    <div className="text-center mt-3">
                        <p className="text-muted small mb-2">Danger Zone</p>
                        <Button variant="outline-danger" size="sm" onClick={handleDeleteAccount}>
                            Deactivate Account
                        </Button>
                    </div>
                </Card.Body>
            </Card>
        </Container>
    );
}

export default MyProfile;
