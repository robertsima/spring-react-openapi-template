import {CircularProgress, Grid2 as Grid, Typography} from "@mui/material";

export const LoadingScreen = ({message = 'Loading...'}: { message?: string }) => {
    return <Grid container direction="column" justifyContent="center" alignItems="center" spacing={2}
                 sx={{minHeight: '100vh'}}>
        <CircularProgress color="secondary"/>
        <Typography color="white">{message}</Typography>
    </Grid>
}
