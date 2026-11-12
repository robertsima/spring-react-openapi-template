import {useNavigate} from "react-router";
import {Alert, Button, Grid2 as Grid, Paper, TextField, Typography} from "@mui/material";
import './HomeScreen.css';
import {FormEvent, useState} from "react";
import {useAuth} from "../../services/Authentication/AuthenticationProvider";
import {ROUTE_ADMIN} from "../../constants/routes";
import {doesUserHaveAnyOneOfTheseRoles, getDisplayName} from "../../utils/auth/auth";
import {ROLE_ADMIN} from "../../constants/roles";
import {GrUserAdmin} from "react-icons/gr";
import {useAppContext} from "../../services/AppContext";
import {ResponseError, UserDTO} from "../../generated/api";
import {logger} from "../../utils/logger";

const HomeScreen = () => {
    const {user} = useAuth();
    const {apis} = useAppContext();
    const navigate = useNavigate();

    const [name, setName] = useState('');
    const [users, setUsers] = useState<UserDTO[]>();
    const [error, setError] = useState<string>();

    // Example call through the generated client - types come straight from openapi/openapi.yaml
    const onSearch = (e: FormEvent) => {
        e.preventDefault();
        setError(undefined);
        apis.usersApi.getUsersByName({name})
            .then(setUsers)
            .catch((err: unknown) => {
                logger.error('User search failed', err);
                setError(`Request failed: ${err instanceof ResponseError ? err.response.status : String(err)}`);
            });
    }

    return <Grid container direction="column" justifyContent="center" alignItems="center" spacing={2}>
        <Grid sx={{marginTop: 16}}>
            <img alt="logo" src={"/logo192.png"}/>
        </Grid>

        <div className="circle-right"/>

        <Typography variant="h5" color="white">Signed in as {getDisplayName(user)}</Typography>

        <Paper elevation={2} sx={{p: 3, minWidth: 360}}>
            <form onSubmit={onSearch}>
                <Grid container spacing={1} alignItems="center">
                    <TextField size="small" label="User name" value={name} required
                               onChange={e => setName(e.target.value)}/>
                    <Button type="submit" variant="contained">Search</Button>
                </Grid>
            </form>
            {error && <Alert severity="error" sx={{mt: 2}}>{error}</Alert>}
            {users && <Typography sx={{mt: 2}}>
                {users.length ? users.map(u => u.name).join(', ') : 'No users found'}
            </Typography>}
        </Paper>

        {doesUserHaveAnyOneOfTheseRoles(user, [ROLE_ADMIN]) &&
            <Grid container justifyContent="center" alignItems={"stretch"}>
                <Button variant={"contained"}
                        onClick={() => navigate(ROUTE_ADMIN)}
                        startIcon={<GrUserAdmin/>}
                        sx={{width: '100%', marginRight: '32px', borderRadius: 2}}
                >Admin Page</Button>
            </Grid>}

        {/* No sign-out with Windows SSO: the session is the user's Windows logon. */}
        <Typography variant="caption" color="grey.500">{user?.username} - {user?.roles.join(', ')}</Typography>
    </Grid>
}

export default HomeScreen;
