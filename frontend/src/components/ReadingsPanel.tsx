import { useMemo, useState } from 'react';
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Snackbar,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  assignReadingToHive,
  createReading,
  deleteReading,
  getHives,
  getReadings,
  updateReading,
} from '../api/hivewatchApi';
import type { TemperatureReading, WriteTemperatureReading } from '../types';
import ReadingFormDialog from './ReadingFormDialog';
import AssignHiveDialog from './AssignHiveDialog';
import { getApiErrorMessage } from '../utils/apiError';
import { toDisplayDateTime } from '../utils/dateTime';

function ReadingsPanel() {
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [assignDialogOpen, setAssignDialogOpen] = useState(false);
  const [editingReading, setEditingReading] = useState<TemperatureReading | null>(null);
  const [assigningReading, setAssigningReading] = useState<TemperatureReading | null>(null);
  const [selectedHiveFilter, setSelectedHiveFilter] = useState<string>('all');
  const [feedback, setFeedback] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');

  const { data: hives = [], isLoading: hivesLoading } = useQuery({
    queryKey: ['hives'],
    queryFn: getHives,
  });

  const { data: readings = [], isLoading: readingsLoading, isError } = useQuery({
    queryKey: ['readings'],
    queryFn: getReadings,
  });

  const filteredReadings = useMemo(() => {
    if (selectedHiveFilter === 'all') {
      return readings;
    }

    return readings.filter((reading) => String(reading.hiveId) === selectedHiveFilter);
  }, [readings, selectedHiveFilter]);

  const createMutation = useMutation({
    mutationFn: createReading,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['readings'] });
      setFeedback('Temperature reading created successfully.');
      setDialogOpen(false);
    },
    onError: (error) => setErrorMessage(getApiErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: WriteTemperatureReading }) =>
      updateReading(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['readings'] });
      setFeedback('Temperature reading updated successfully.');
      setDialogOpen(false);
      setEditingReading(null);
    },
    onError: (error) => setErrorMessage(getApiErrorMessage(error)),
  });

  const deleteMutation = useMutation({
    mutationFn: deleteReading,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['readings'] });
      setFeedback('Temperature reading deleted successfully.');
    },
    onError: (error) => setErrorMessage(getApiErrorMessage(error)),
  });

  const assignMutation = useMutation({
    mutationFn: ({ readingId, hiveId }: { readingId: number; hiveId: number }) =>
      assignReadingToHive(readingId, hiveId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['readings'] });
      setFeedback('Reading reassigned to a different hive.');
      setAssignDialogOpen(false);
      setAssigningReading(null);
    },
    onError: (error) => setErrorMessage(getApiErrorMessage(error)),
  });

  const handleSave = (payload: WriteTemperatureReading) => {
    setErrorMessage('');

    if (editingReading) {
      updateMutation.mutate({ id: editingReading.id, payload });
      return;
    }

    createMutation.mutate(payload);
  };

  if (hivesLoading || readingsLoading) {
    return (
      <Box sx={{ textAlign: 'center', py: 6 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (isError) {
    return <Alert severity="error">Unable to load temperature readings.</Alert>;
  }

  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} justifyContent="space-between">
        <Typography variant="body1">
          Total readings shown: <strong>{filteredReadings.length}</strong>
        </Typography>

        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <FormControl size="small" sx={{ minWidth: 220 }}>
            <InputLabel id="reading-filter-label">Filter by hive</InputLabel>
            <Select
              labelId="reading-filter-label"
              label="Filter by hive"
              value={selectedHiveFilter}
              onChange={(event) => setSelectedHiveFilter(String(event.target.value))}
            >
              <MenuItem value="all">All hives</MenuItem>
              {hives.map((hive) => (
                <MenuItem key={hive.id} value={String(hive.id)}>
                  {hive.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          <Button
            variant="contained"
            onClick={() => {
              setEditingReading(null);
              setErrorMessage('');
              setDialogOpen(true);
            }}
          >
            Add Reading
          </Button>
        </Stack>
      </Stack>

      {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Temperature</TableCell>
              <TableCell>Recorded At</TableCell>
              <TableCell>Hive</TableCell>
              <TableCell align="right">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredReadings.map((reading) => (
              <TableRow key={reading.id}>
                <TableCell>{reading.id}</TableCell>
                <TableCell>{reading.temperature} °C</TableCell>
                <TableCell>{toDisplayDateTime(reading.recordedAt)}</TableCell>
                <TableCell>{reading.hiveName || `Hive ${reading.hiveId}`}</TableCell>
                <TableCell align="right">
                  <Stack direction="row" spacing={1} justifyContent="flex-end" flexWrap="wrap">
                    <Button
                      size="small"
                      variant="outlined"
                      onClick={() => {
                        setEditingReading(reading);
                        setErrorMessage('');
                        setDialogOpen(true);
                      }}
                    >
                      Edit
                    </Button>
                    <Button
                      size="small"
                      variant="outlined"
                      onClick={() => {
                        setAssigningReading(reading);
                        setErrorMessage('');
                        setAssignDialogOpen(true);
                      }}
                    >
                      Assign Hive
                    </Button>
                    <Button
                      size="small"
                      color="error"
                      variant="outlined"
                      onClick={() => {
                        if (window.confirm(`Delete reading ${reading.id}?`)) {
                          setErrorMessage('');
                          deleteMutation.mutate(reading.id);
                        }
                      }}
                    >
                      Delete
                    </Button>
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <ReadingFormDialog
        open={dialogOpen}
        reading={editingReading}
        hives={hives}
        saving={createMutation.isLoading || updateMutation.isLoading}
        onClose={() => {
          setDialogOpen(false);
          setEditingReading(null);
        }}
        onSave={handleSave}
      />

      <AssignHiveDialog
        open={assignDialogOpen}
        reading={assigningReading}
        hives={hives}
        saving={assignMutation.isLoading}
        onClose={() => {
          setAssignDialogOpen(false);
          setAssigningReading(null);
        }}
        onSave={(hiveId) => {
          if (!assigningReading) return;
          setErrorMessage('');
          assignMutation.mutate({ readingId: assigningReading.id, hiveId });
        }}
      />

      <Snackbar open={Boolean(feedback)} autoHideDuration={2500} onClose={() => setFeedback('')}>
        <Alert severity="success" onClose={() => setFeedback('')}>
          {feedback}
        </Alert>
      </Snackbar>
    </Stack>
  );
}

export default ReadingsPanel;
