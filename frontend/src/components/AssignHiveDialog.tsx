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
  Typography,
} from '@mui/material';
import type { Hive, TemperatureReading } from '../types';

interface AssignHiveDialogProps {
  open: boolean;
  reading: TemperatureReading | null;
  hives: Hive[];
  saving?: boolean;
  onClose: () => void;
  onSave: (hiveId: number) => void;
}

function AssignHiveDialog({
  open,
  reading,
  hives,
  saving = false,
  onClose,
  onSave,
}: AssignHiveDialogProps) {
  const [selectedHiveId, setSelectedHiveId] = useState<string>('');

  useEffect(() => {
    setSelectedHiveId('');
  }, [open, reading]);

  const availableHives = hives.filter((hive) => hive.id !== reading?.hiveId);

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Assign reading to a different hive</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Select a new hive for this reading. This updates which hive the reading is associated with.
          </Typography>

          <Typography variant="body2">
            <strong>Reading:</strong> {reading?.id} | <strong>Current hive:</strong> {reading?.hiveName}
          </Typography>

          <FormControl fullWidth>
            <InputLabel id="assign-hive-label">New hive</InputLabel>
            <Select
              labelId="assign-hive-label"
              label="New hive"
              value={selectedHiveId}
              onChange={(event) => setSelectedHiveId(String(event.target.value))}
            >
              {availableHives.map((hive) => (
                <MenuItem key={hive.id} value={String(hive.id)}>
                  {hive.name} - {hive.location}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          variant="contained"
          disabled={!selectedHiveId || saving}
          onClick={() => onSave(Number(selectedHiveId))}
        >
          Assign Hive
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export default AssignHiveDialog;
