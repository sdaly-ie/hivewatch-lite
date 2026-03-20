import { useEffect, useState } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from '@mui/material';
import type { Hive, WriteHive } from '../types';

interface HiveFormDialogProps {
  open: boolean;
  hive?: Hive | null;
  saving?: boolean;
  onClose: () => void;
  onSave: (payload: WriteHive) => void;
}

const emptyForm: WriteHive = {
  name: '',
  location: '',
};

function HiveFormDialog({ open, hive, saving = false, onClose, onSave }: HiveFormDialogProps) {
  const [form, setForm] = useState<WriteHive>(emptyForm);

  useEffect(() => {
    if (hive) {
      setForm({
        name: hive.name,
        location: hive.location,
      });
      return;
    }

    setForm(emptyForm);
  }, [hive, open]);

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{hive ? 'Edit hive' : 'Add new hive'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label="Hive name"
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            placeholder="Example: Hive C"
            fullWidth
          />
          <TextField
            label="Location"
            value={form.location}
            onChange={(event) => setForm({ ...form, location: event.target.value })}
            placeholder="Example: Orchard"
            fullWidth
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" onClick={() => onSave(form)} disabled={saving}>
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default HiveFormDialog;
