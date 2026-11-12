import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Plus, Edit, Trash2, Users as UsersIcon } from 'lucide-react';
import { customerApi } from '../api/services';
import type { Customer } from '../types';
import Loading from '../components/Loading';
import ErrorMessage from '../components/ErrorMessage';
import EmptyState from '../components/EmptyState';

export default function Customers() {
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState('');

  const loadCustomers = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await customerApi.getAll({ page, size: 20, sortBy: 'id', direction: 'DESC' });
      setCustomers(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (err: any) {
      setError(err.message || 'Failed to load customers');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCustomers();
  }, [page]);

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this customer?')) return;
    
    try {
      await customerApi.delete(id);
      loadCustomers();
    } catch (err: any) {
      alert(err.message || 'Failed to delete customer');
    }
  };

  const filteredCustomers = customers.filter(customer =>
    `${customer.firstName} ${customer.lastName} ${customer.phone} ${customer.email}`
      .toLowerCase()
      .includes(searchTerm.toLowerCase())
  );

  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} onRetry={loadCustomers} />;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Customers</h2>
          <p className="text-gray-600 mt-1">Manage your customer database</p>
        </div>
        <Link to="/customers/new" className="btn-primary inline-flex items-center space-x-2">
          <Plus className="w-5 h-5" />
          <span>Add Customer</span>
        </Link>
      </div>

      {/* Search */}
      <div className="card">
        <input
          type="text"
          placeholder="Search customers by name, phone, or email..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="input-field"
        />
      </div>

      {/* Customers List */}
      {filteredCustomers.length === 0 ? (
        <EmptyState
          icon={UsersIcon}
          title="No customers found"
          description="Get started by adding your first customer"
          action={{ label: 'Add Customer', onClick: () => window.location.href = '/customers/new' }}
        />
      ) : (
        <div className="card">
          <div className="table-wrapper">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Customer
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Phone
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Email
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Created
                  </th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredCustomers.map((customer) => (
                  <tr key={customer.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <div className="font-medium text-gray-900">
                          {customer.firstName} {customer.lastName}
                        </div>
                        <div className="text-sm text-gray-500">ID: {customer.id}</div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {customer.phone}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {customer.email || '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(customer.createdAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
                      <Link
                        to={`/customers/${customer.id}/edit`}
                        className="text-primary-600 hover:text-primary-900 inline-flex items-center"
                      >
                        <Edit className="w-4 h-4" />
                      </Link>
                      <button
                        onClick={() => handleDelete(customer.id)}
                        className="text-red-600 hover:text-red-900 inline-flex items-center"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between px-6 py-4 border-t border-gray-200">
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                className="btn-secondary"
              >
                Previous
              </button>
              <span className="text-sm text-gray-700">
                Page {page + 1} of {totalPages}
              </span>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
                className="btn-secondary"
              >
                Next
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
