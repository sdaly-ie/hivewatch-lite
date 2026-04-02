import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import HiveFormDialog from './HiveFormDialog';

describe('HiveFormDialog', () => {
  it('renders add mode fields and buttons', () => {
    render(
      <HiveFormDialog
        open={true}
        onClose={vi.fn()}
        onSave={vi.fn()}
      />
    );

    expect(screen.getByText(/add new hive/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/location/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /save/i })).toBeInTheDocument();
  });

  it('pre-fills the form when editing an existing hive', async () => {
    render(
      <HiveFormDialog
        open={true}
        hive={{ id: 1, name: 'Hive A', location: 'Orchard' }}
        onClose={vi.fn()}
        onSave={vi.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByDisplayValue('Hive A')).toBeInTheDocument();
      expect(screen.getByDisplayValue('Orchard')).toBeInTheDocument();
    });
  });

  it('calls onSave with the values entered by the user', async () => {
    const user = userEvent.setup();
    const onSave = vi.fn();

    render(
      <HiveFormDialog
        open={true}
        onClose={vi.fn()}
        onSave={onSave}
      />
    );

    await user.type(screen.getByLabelText(/name/i), 'Hive C');
    await user.type(screen.getByLabelText(/location/i), 'Orchard');
    await user.click(screen.getByRole('button', { name: /save/i }));

    await waitFor(() => {
      expect(onSave).toHaveBeenCalledWith({
        name: 'Hive C',
        location: 'Orchard',
      });
    });
  });
});