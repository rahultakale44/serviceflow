import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { customerApi } from '../api/services';
import type { CustomerRequest } from '../types';

export default function CustomerForm() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<CustomerRequest>({
    firstName: '',
    lastName: '',
    phone: '',
    email: '',
  });

  useEffect(() => {
    if (id) {
      loadCustomer(Number(id));
    }
  }, [id]);

  const loadCustomer = async (customerId: number) => {
    try {
      const response = await customerApi.getById(customerId);
      setFormData({
        firstName: response.data.firstName,
        lastName: response.data.lastName,
        phone: response.data.phone,
        email: response.data.email || '',
      });
    } catch (err) {
      alert('Failed to load customer');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      if (id) {
        await customerApi.update(Number(id), formData);
      } else {
        await customerApi.create(formData);
      }
      navigate('/customers');
    } catch (err: any) {
      alert(err.message || 'Failed to save customer');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl">
      <h2 className="text-2xl font-bold mb-6">{id ? 'Edit' : 'Add'} Customer</h2>
      
      <form onSubmit={handleSubmit} className="card space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            First Name *
          </label>
          <input
            type="text"
            required
            className="input-field"
            value={formData.firstName}
            onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Last Name *
          </label>
          <input
            type="text"
            required
            className="input-field"
            value={formData.lastName}
            onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Phone *
          </label>
          <input
            type="tel"
            required
            className="input-field"
            value={formData.phone}
            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
          />
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Email
          </label>
          <input
            type="email"
            className="input-field"
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
          />
        </div>

        <div className="flex space-x-3 pt-4">
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Saving...' : 'Save'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/customers')}
            className="btn-secondary"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
