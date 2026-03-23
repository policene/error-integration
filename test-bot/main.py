import json
import discord
from discord.ext import commands
import os
from dotenv import load_dotenv
import redis


load_dotenv()

intents = discord.Intents.default()
intents.message_content = True

bot = commands.Bot(command_prefix='!', intents=intents)

r = redis.Redis(
    host=os.getenv('REDIS_HOST'), 
    port=os.getenv('REDIS_PORT'), 
    decode_responses=True
)

pubsub = r.pubsub()


async def listen_redis():
    pubsub.subscribe(os.getenv('REDIS_CHANNEL'))
    channel = bot.get_channel(int(os.getenv('CHANNEL_ID')))
    
    while True:
        message = pubsub.get_message()
        if message and message['type'] == 'message':
            print(f"Mensagem recebida: {message['data']}")
            error = json.loads(message['data'])
            errorMessage = error['message']
            errorCause = error['cause']
            errorCount = error['count']
            isCritical = error['critical']

            embedColor = 0xED4245 if isCritical else 0xFFC300 
            errorType = "Crítico" if isCritical else "Alerta"
            
            embed = discord.Embed(
                title="Erro de Produção",
                color=embedColor 
            )
            
            embed.add_field(name="Ocorrências", value=errorCount, inline=True)
            embed.add_field(name="Tipo", value=errorType, inline=True)
            embed.add_field(name="Mensagem", value=errorMessage, inline=False)
            embed.add_field(name="Causa", value=errorCause, inline=False)
            await channel.send(embed=embed)

@bot.event
async def on_ready():
    print(f"Bot online.")
    bot.loop.create_task(listen_redis())


bot.run(os.getenv('BOT_TOKEN'))