from gevent import monkey; monkey.patch_all()

from socketio import socketio_manage
from socketio.server import SocketIOServer
from socketio.namespace import BaseNamespace
from socketio.mixins import RoomsMixin, BroadcastMixin

import random


class SwanNamespace(BaseNamespace, RoomsMixin, BroadcastMixin):

    player_sessid = [];
    game_player_sessid = [];
    count = 0;
    screenSocket = None
    hostSocket = None
    acks_before_game_start = 0;

#Connection/Disconnection Code
################################################################################################
################################################################################################
################################################################################################
    def on_screen_set(self):
        print 'Screen has connected'
        self.socket.session['nickname'] = 'Screen'
        SwanNamespace.screenSocket = self.socket

    def on_nickname_set(self, nickname):
        if nickname in self.request['nicknames']:
            print 'Player attempted to join with duplicate nickname: %s, telling player to retry with new name' % nickname
            self.emit('invalid_nickname')
            return

        self.request['nicknames'].append(nickname)
        print self.request['nicknames']
        self.socket.session['nickname'] = nickname
        
        self.broadcast_event('announcement', '%s has connected' % nickname)
        
        if (SwanNamespace.count == 0):
            SwanNamespace.hostSocket = self.socket
            self.emit('elected_host')
            print 'Host is', nickname
        else:
            print 'client is', nickname
            self.emit('elected_client')
        
        SwanNamespace.count = SwanNamespace.count + 1
        # Just have them join a default-named room
        self.join('main_room')

        SwanNamespace.player_sessid.append([self.socket, nickname])
        print SwanNamespace.player_sessid


    def on_game_start(self):
        print 'Game started request received. Waiting on acks'
        SwanNamespace.acks_before_game_start = list(self.request['nicknames']) + ['Screen']
        self.broadcast_event('game_start')

    def on_player_ready(self, nickname):
        SwanNamespace.acks_before_game_start.remove(nickname)
        if len(SwanNamespace.acks_before_game_start) == 0:
            print 'Everyone ready to start'
            self.broadcast_event('everyone_ready')
        else:
            print "%s is ready, still waiting on %s" % (nickname, SwanNamespace.acks_before_game_start)

    def recv_disconnect(self):
        if not self.socket.session.has_key('nickname'):
            print 'An unidentified player (no nickname set) has disconnected'
            return 

        # Remove nickname from the list.
        nickname = self.socket.session['nickname']
        
        #broadcast to everyone that someone has disconnected
        self.broadcast_event('client_disconnect', nickname)
        self.broadcast_event('announcement', '%s has disconnected' % nickname)
        
        if nickname != 'Screen':
            SwanNamespace.player_sessid.remove([self.socket, nickname])
            self.request['nicknames'].remove(nickname)
            SwanNamespace.count = SwanNamespace.count - 1

        print "%s Has Disconnected" % nickname
        print SwanNamespace.player_sessid

        #elect a new host
        if self.socket == SwanNamespace.hostSocket:
            if SwanNamespace.count != 0:
                self.emit_to_socket('elected_host', SwanNamespace.player_sessid[0][0])
                SwanNamespace.hostSocket = SwanNamespace.player_sessid[0][0]
                self.broadcast_event('announcement', '%s is now the host' % SwanNamespace.player_sessid[0][1])
            else:
                print "Waiting for Host to Connect"
        self.disconnect(silent=True)

################################################################################################
################################################################################################
################################################################################################


#Mailbox Implementation
################################################################################################
################################################################################################
################################################################################################

    def on_swan_broadcast(self, event, args):
        print "BROADCASTING EVENT ", event, " WITH ARGS ", args 
        self.broadcast_event(event, *args)

    def on_swan_emit(self, nickname, event, args):
        print "SENDING TO ", nickname, " EVENT ", event, " WITH ARGS ", args
        self.emit_to_nickname(nickname, event, args)

    def on_swan_get_nicknames(self):
        self.emit_to_socket("swan_get_nicknames", self.socket, self.request['nicknames'])

################################################################################################
################################################################################################
################################################################################################



#Misc Functions
################################################################################################
################################################################################################
################################################################################################ 

    def recv_message(self, message):
        print "PING!!!", message

    def emit_to_nickname(self, name, event, args):
        if name == "Screen":
            self.emit_to_socket(event, SwanNamespace.screenSocket, *args)
        else:
            for index, nickname in enumerate(SwanNamespace.player_sessid):
                #check if nickname is the same as in the list
                if nickname[1] == name:
                    self.emit_to_socket(event, SwanNamespace.player_sessid[index][0], *args)

    def emit_to_socket(self, event, target, *args):
        pkt = dict(type="event",
                    name=event,
                    args=args,
                    endpoint=self.ns_name)
        
        for sessid, socket in self.socket.server.sockets.iteritems():
            if socket is target:
                socket.send_packet(pkt)
                break


################################################################################################
################################################################################################
################################################################################################ 

class Application(object):
    def __init__(self):
        self.buffer = []
        # Dummy request object to maintain state between Namespace
        # initialization.
        self.request = {
            'nicknames': [], 
        }

        self.config = {
            'heartbeat_timeout': 5,
            'close_timeout': 6,
            'heartbeat_interval': 1,
        }



    def __call__(self, environ, start_response):
        path = environ['PATH_INFO'].strip('/')

        if not path:
            start_response('200 OK', [('Content-Type', 'text/html')])
            return ['<h1>Welcome. '
                'Try the <a href="/chat.html">chat</a> example.</h1>']

        if path.startswith('static/') or path == 'chat.html':
            try:
                data = open(path).read()
            except Exception:
                return not_found(start_response)

            if path.endswith(".js"):
                content_type = "text/javascript"
            elif path.endswith(".css"):
                content_type = "text/css"
            elif path.endswith(".swf"):
                content_type = "application/x-shockwave-flash"
            else:
                content_type = "text/html"

            start_response('200 OK', [('Content-Type', content_type)])
            return [data]

        if path.startswith("socket.io"):
            socketio_manage(environ, {'': SwanNamespace}, self.request)
        else:
            return not_found(start_response)


def not_found(start_response):
    start_response('404 Not Found', [])
    return ['<h1>Not Found</h1>']


if __name__ == '__main__':
    print 'Listening on port 8080 and on port 843 (flash policy server)'
    SocketIOServer(('0.0.0.0', 8080), Application(),
        resource="socket.io", policy_server=True,
        policy_listener=('0.0.0.0', 10843)).serve_forever()