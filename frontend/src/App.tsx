import { useMemo, useState } from 'react';
import {
  Alert,
  AppBar,
  Box,
  Container,
  Paper,
  Tab,
  Tabs,
  Toolbar,
  Typography,
} from '@mui/material';
import HivesPanel from './components/HivesPanel';
import ReadingsPanel from './components/ReadingsPanel';

function App() {
  const [tab, setTab] = useState(0);

  const title = useMemo(() => {
    return tab === 0 ? 'Hive management' : 'Temperature reading management';
  }, [tab]);

  return (
    <Box>
      <AppBar position="static">
        <Toolbar>
          <Box>
            <Typography variant="h6">HiveWatch Lite</Typography>
            <Typography variant="body2">Hive monitoring dashboard</Typography>
          </Box>
        </Toolbar>
      </AppBar>

      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="info" sx={{ mb: 3 }}>
          Start the Spring Boot back end first, then run this React app. The default API URL is
          <strong> http://localhost:8080</strong>.
        </Alert>

        <Paper sx={{ overflow: 'hidden' }}>
          <Tabs value={tab} onChange={(_, value) => setTab(value)}>
            <Tab label="Hives" />
            <Tab label="Temperature Readings" />
          </Tabs>

          <Box sx={{ p: 3 }}>
            <Typography variant="h5" sx={{ mb: 1 }}>
              {title}
            </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                Manage hives, record temperature readings, and update the hive associated with a reading.
              </Typography>

            {tab === 0 ? <HivesPanel /> : <ReadingsPanel />}
          </Box>
        </Paper>
      </Container>
    </Box>
  );
}

export default App;
