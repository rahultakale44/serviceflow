import { AlertCircle } from 'lucide-react';

interface ErrorMessageProps {
  message: string;
  onRetry?: () => void;
}

export default function ErrorMessage({ message, onRetry }: ErrorMessageProps) {
  return (
    <div className="flex flex-col items-center justify-center min-h-[400px]">
      <div className="bg-red-50 border border-red-200 rounded-lg p-6 max-w-md">
        <div className="flex items-start space-x-3">
          <AlertCircle className="w-6 h-6 text-red-600 flex-shrink-0" />
          <div className="flex-1">
            <h3 className="text-lg font-semibold text-red-900 mb-2">Error</h3>
            <p className="text-red-700 mb-4">{message}</p>
            {onRetry && (
              <button onClick={onRetry} className="btn-secondary">
                Try Again
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
