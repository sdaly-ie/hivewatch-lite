import { useEffect, useState } from 'react';
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
} from '@mui/material';
import type { Hive, TemperatureReading, WriteTemperatureReading } from '../types';
import { toDateTimeLocalValue } from '../utils/dateTime';

interface ReadingFormDialogProps {
  open: boolean;
  reading?: TemperatureReading | null;
  hives: Hive[];
  saving?: boolean;
  onClose: () => void;
  onSave: (payload: WriteTemperatureReading) => void;
}

type ReadingFormState = {
  temperature: string;
  recordedAt: string;
  hiveId: string;
};

const emptyForm: ReadingFormState = {
  temperature: '',
  recordedAt: '',
  hiveId: '',
};

function ReadingFormDialog({
  open,
  reading,
  hives,
  saving = false,
  onClose,
  onSave,
}: ReadingFormDialogProps) {
  const [form, setForm] = useState<ReadingFormState>(emptyForm);

  useEffect(() => {
    if (reading) {
      setForm({
        temperature: String(reading.temperature),
        recordedAt: toDateTimeLocalValue(reading.recordedAt),
        hiveId: String(reading.hiveId),
      });
      return;
    }

    setForm(emptyForm);
  }, [reading, open]);

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{reading ? 'Edit temperature reading' : 'Add new temperature reading'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            type="number"
            label="Temperature"
            inputProps={{ step: '0.1' }}
            value={form.temperature}
            onChange={(event) => setForm({ ...form, temperature: event.target.value })}
            placeholder="Example: 34.2"
            fullWidth
          />

          <TextField
            type="datetime-local"
            label="Recorded at"
            InputLabelProps={{ shrink: true }}
            value={form.recordedAt}
            onChange={(event) => setForm({ ...form, recordedAt: event.target.value })}
            fullWidth
          />

          <FormControl fullWidth>
            <InputLabel id="hive-select-label">Hive</InputLabel>
            <Select
              labelId="hive-select-label"
              label="Hive"
              value={form.hiveId}
              disabled={Boolean(reading)}
              onChange={(event) => setForm({ ...form, hiveId: String(event.target.value) })}
            >
              {hives.map((hive) => (
                <MenuItem key={hive.id} value={String(hive.id)}>
                  {hive.name} - {hive.location}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          {reading && (
            <Typography variant="body2" color="text.secondary">
              The hive cannot be changed from this form. Use the <strong>Assign Hive</strong> button to move this reading to a different hive.
            </Typography>
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          variant="contained"
          onClick={() =>
            onSave({
              temperature: Number(form.temperature),
              recordedAt: form.recordedAt,
              hiveId: Number(form.hiveId),
            })
          }
          disabled={saving}
        >
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default ReadingFormDialog;
