import {
    Grid2 as Grid, Paper, Switch, Typography,
} from "@mui/material";
import '../HomeScreen/HomeScreen.css';
import {BackButton} from "../../components/navigation/BackButton";
import {useState} from "react";
import {WebSocketEventFromJSON} from "../../generated/api";
import {WS_TOPIC_MY_EVENTS} from "../../constants/websockets";
import {useSubscription} from "react-stomp-hooks";



const AdminScreen = () => {

    const [status,setStatus] = useState<boolean>(false);

    const onMessage = (msg: {body:string}) => {
        // WebSocketEvent is defined in openapi/openapi.yaml, so this type is shared with the backend
        const event = WebSocketEventFromJSON(JSON.parse(msg.body));
        setStatus(event.status);
    }

    useSubscription(WS_TOPIC_MY_EVENTS, onMessage);

    return <Grid
        container
        direction="column"
        justifyContent="center"
        alignItems="center"
        spacing={2}
    >

        <BackButton/>

        <Grid sx={{marginTop: 16}}>
            <img alt="logo" src={"/logo192.png"}/>
        </Grid>

        <div className="circle-right"/>

        <Grid container justifyContent="center" alignItems={"stretch"} >
            <Paper elevation={2} sx={{p:5}}>
                <Typography> This is the admin page. The switch below is being controlled by web socket events from the server</Typography>
                <Switch checked={status}/>
            </Paper>
        </Grid>

    </Grid>
}

export default AdminScreen;
