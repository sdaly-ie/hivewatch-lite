import { useState } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Paper,
  Snackbar,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Alert,
} from '@mui/material';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createHive, deleteHive, getHives, updateHive } from '../api/hivewatchApi';
import type { Hive, WriteHive } from '../types';
import HiveFormDialog from './HiveFormDialog';
import { getApiErrorMessage } from '../utils/apiError';

function HivesPanel() {
  const queryClient = useQueryClient();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingHive, setEditingHive] = useState<Hive | null>(null);
  const [feedback, setFeedback] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');

  const { data: hives = [], isLoading, isError } = useQuery({
    queryKey: ['hives'],
    queryFn: getHives,
  });

  const createMutation = useMutation({
    mutationFn: createHive,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hives'] });
      setFeedback('Hive created successfully.');
      setDialogOpen(false);
    },
    onError: (error) => setErrorMessage(getApiErrorMessage(error)),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }: { id: number; payload: WriteHive }) => updateHive(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hives'] });
      queryClient.invalidateQueries({ queryKey: ['readings'] });
      setFeedback('Hive updated successfully.');
      setDialogOpen(false);
      setEditingHive(null);
    },
    onError: (error) => setErrorMessage(getApiErrorMessage(error)),
  });

  const deleteMutation = useMutation({
    mutationFn: deleteHive,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hives'] });
      setFeedback('Hive deleted successfully.');
    },
    onError: (error) => setErrorMessage(getApiErrorMessage(error)),
  });

  const isSaving = createMutation.isLoading || updateMutation.isLoading;
  const isDeleting = deleteMutation.isLoading;
  const isBusy = isSaving || isDeleting;

  const handleSave = (payload: WriteHive) => {
    setErrorMessage('');

    if (editingHive) {
      updateMutation.mutate({ id: editingHive.id, payload });
      return;
    }

    createMutation.mutate(payload);
  };

  if (isLoading) {
    return (
      <Box sx={{ textAlign: 'center', py: 6 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (isError) {
    return <Alert severity="error">Unable to load hives.</Alert>;
  }

  return (
    <Stack spacing={2}>
      <Stack direction={{ xs: 'column', sm: 'row' }} justifyContent="space-between" spacing={2}>
        <Typography variant="body1">
          Total hives: <strong>{hives.length}</strong>
        </Typography>
        <Button
          variant="contained"
          disabled={isBusy}
          onClick={() => {
            setEditingHive(null);
            setErrorMessage('');
            setDialogOpen(true);
          }}
        >
          {isSaving ? 'Saving...' : isDeleting ? 'Deleting...' : 'Add Hive'}
        </Button>
      </Stack>

      {errorMessage && <Alert severity="error">{errorMessage}</Alert>}

      {hives.length === 0 ? (
        <Paper variant="outlined" sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h6" gutterBottom>
            No hives yet
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Click &quot;Add Hive&quot; to create your first hive.
          </Typography>
        </Paper>
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Location</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {hives.map((hive) => (
                <TableRow key={hive.id}>
                  <TableCell>{hive.id}</TableCell>
                  <TableCell>{hive.name}</TableCell>
                  <TableCell>{hive.location}</TableCell>
                  <TableCell align="right">
                    <Stack direction="row" spacing={1} justifyContent="flex-end">
                      <Button
                        size="small"
                        variant="outlined"
                        disabled={isBusy}
                        onClick={() => {
                          setEditingHive(hive);
                          setErrorMessage('');
                          setDialogOpen(true);
                        }}
                      >
                        Edit
                      </Button>
                      <Button
                        size="small"
                        color="error"
                        variant="outlined"
                        disabled={isBusy}
                        onClick={() => {
                          if (
                            window.confirm(
                              `Delete hive ${hive.name}? This will fail if readings still exist for it.`,
                            )
                          ) {
                            setErrorMessage('');
                            deleteMutation.mutate(hive.id);
                          }
                        }}
                      >
                        {isDeleting ? 'Deleting...' : 'Delete'}
                      </Button>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <HiveFormDialog
        open={dialogOpen}
        hive={editingHive}
        saving={isSaving}
        onClose={() => {
          setDialogOpen(false);
          setEditingHive(null);
        }}
        onSave={handleSave}
      />

      <Snackbar open={Boolean(feedback)} autoHideDuration={2500} onClose={() => setFeedback('')}>
        <Alert severity="success" onClose={() => setFeedback('')}>
          {feedback}
        </Alert>
      </Snackbar>
    </Stack>
  );
}

export default HivesPanel;