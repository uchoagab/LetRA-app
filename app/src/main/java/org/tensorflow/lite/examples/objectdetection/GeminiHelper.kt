package org.tensorflow.lite.examples.objectdetection

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.QuotaExceededException
import kotlinx.coroutines.delay
import org.json.JSONObject

data class ConteudoEducacional(val palavraTraduzida: String, val silabas: String, val frases: List<String>)

sealed class GeminiResult {
    data class Success(val conteudo: ConteudoEducacional) : GeminiResult()
    data class Error(val message: String, val isQuotaError: Boolean = false) : GeminiResult()
}

private val dicionarioMock = mapOf(
    "person" to ConteudoEducacional(
        palavraTraduzida = "Pessoa",
        silabas = "Pes-so-a",
        frases = listOf(
            "Aquela pessoa é simpática.",
            "Cada pessoa tem uma história.",
            "Cada dia aparece uma pessoa mais estranha aqui."
        )
    ),
    "mouse" to ConteudoEducacional(
        palavraTraduzida = "Mouse",
        silabas = "Mou-se",
        frases = listOf(
            "Coloque o mouse na mesa.",
            "Quero aprender a usar o mouse.",
            "O mouse quebrou e ninguém sabe concertar."
        )
    ),
    "bicycle" to ConteudoEducacional(
        palavraTraduzida = "Bicicleta",
        silabas = "Bi-ci-cle-ta",
        frases = listOf(
            "Eu comprei uma bicicleta nova.",
            "A bicicleta está na garagem.",
            "Aprendi a andar de bicileta contigo."
        )
    ),
    "car" to ConteudoEducacional(
        palavraTraduzida = "Carro",
        silabas = "Car-ro",
        frases = listOf(
            "Eu tenho um carro azul.",
            "O carro está estacionado lá fora.",
            "Ela sempre quis comprar um carro alto."
        )
    ),
    "motorcycle" to ConteudoEducacional(
        palavraTraduzida = "Motoc",
        silabas = "Mo-to",
        frases = listOf(
            "Eu tenho uma motoc rápida.",
            "A motoc está na rua.",
            "Meu amigo caiu da moto mas não teve nada grave."
        )
    ),
    "airplane" to ConteudoEducacional(
        palavraTraduzida = "Avião",
        silabas = "A-vi-ão",
        frases = listOf(
            "Eu viajei de avião ontem.",
            "O avião pousou no aeroporto.",
            "Esse avião é muito chique mesmo."
        )
    ),
    "bus" to ConteudoEducacional(
        palavraTraduzida = "Ônibus",
        silabas = "Ô-ni-bus",
        frases = listOf(
            "Eu peguei o ônibus para o trabalho.",
            "O ônibus está atrasado hoje.",
            "O motorista do ônibus pulou minha parada."
        )
    ),
    "train" to ConteudoEducacional(
        palavraTraduzida = "Trem",
        silabas = "Trem",
        frases = listOf(
            "Eu viajei de trem no fim de semana.",
            "O trem chegou na estação.",
            "Algum dia vão botar o trem pra funcionar novamente."
        )
    ),
    "truck" to ConteudoEducacional(
        palavraTraduzida = "Caminhão",
        silabas = "Ca-mi-nhão",
        frases = listOf(
            "O caminhão transporta mercadorias.",
            "O caminhão está na estrada.",
            "Meu pai só para de dirigr caminhão quando morrer."
        )
    ),
    "boat" to ConteudoEducacional(
        palavraTraduzida = "Barco",
        silabas = "Bar-co",
        frases = listOf(
            "Eu vi um barco no lago.",
            "O barco está ancorado no porto.",
            "Minha namorada me levou pra andar de barco no Antigo."
        )
    ),
    "traffic light" to ConteudoEducacional(
        palavraTraduzida = "Semáforo",
        silabas = "Se-má-fo-ro",
        frases = listOf(
            "O semáforo está vermelho.",
            "Espere o semáforo abrir para atravessar.",
            "Aquele semáforo da Rui Barbosa fecha nunca."
        )
    ),
    "fire hydrant" to ConteudoEducacional(
        palavraTraduzida = "Hidrante",
        silabas = "Hi-dran-te",
        frases = listOf(
            "O hidrante está na calçada.",
            "Os bombeiros usam o hidrante.",
            "A gente já não usa hidrante faz muito tempo."
        )
    ),
    "stop sign" to ConteudoEducacional(
        palavraTraduzida = "Placa de Pare",
        silabas = "Pla-ca de Pa-re",
        frases = listOf(
            "A placa de pare está na esquina.",
            "Pare na placa para sua segurança.",
            "Ele reprovou porquê desrespeitou a placa de pare."
        )
    ),
    "parking meter" to ConteudoEducacional(
        palavraTraduzida = "Parquímetro",
        silabas = "Par-quí-me-tro",
        frases = listOf(
            "Coloque moedas no parquímetro.",
            "O parquímetro está funcionando.",
            "Ele pagou o parquímetro para estacionar."
        )
    ),
    "bench" to ConteudoEducacional(
        palavraTraduzida = "Banco",
        silabas = "Ban-co",
        frases = listOf(
            "O banco da praça é confortável.",
            "Eu sentei no banco para descansar.",
            "Não tinha um lugar melhor pra colocar o banco?."
        )
    ),
    "bird" to ConteudoEducacional(
        palavraTraduzida = "Pássaro",
        silabas = "Pás-sa-ro",
        frases = listOf(
            "O pássaro canta pela manhã.",
            "Eu vi um pássaro no jardim.",
            "Meu avô criava cada pássaro bonito"
        )
    ),
    "cat" to ConteudoEducacional(
        palavraTraduzida = "Gato",
        silabas = "Ga-to",
        frases = listOf(
            "Eu tenho um gato preto.",
            "O gato dorme no sofá.",
            "Meu gato está comigo faz mais de cinco anos."
        )
    ),
    "dog" to ConteudoEducacional(
        palavraTraduzida = "Cachorro",
        silabas = "Ca-chor-ro",
        frases = listOf(
            "Eu levo o cachorro para passear.",
            "O cachorro está no quintal.",
            "Ele não pode ver um cachorro que quer adotar."
        )
    ),
    "horse" to ConteudoEducacional(
        palavraTraduzida = "Cavalo",
        silabas = "Ca-va-lo",
        frases = listOf(
            "O cavalo correu pelo campo.",
            "Eu montei no cavalo ontem.",
            "Faz muito tempo que andei de cavalo."
        )
    ),
    "sheep" to ConteudoEducacional(
        palavraTraduzida = "Ovelha",
        silabas = "O-ve-lha",
        frases = listOf(
            "A ovelha está no pasto.",
            "Eu vi uma ovelha no campo.",
            "Por aqui é difícil criar ovelha."
        )
    ),
    "cow" to ConteudoEducacional(
        palavraTraduzida = "Vaca",
        silabas = "Va-ca",
        frases = listOf(
            "A vaca dá leite.",
            "Eu vi uma vaca no sítio.",
            "Lá no sítio eles criam vaca."
        )
    ),
    "elephant" to ConteudoEducacional(
        palavraTraduzida = "Elefante",
        silabas = "E-le-fan-te",
        frases = listOf(
            "Eu vi um elefante no zoológico.",
            "Ele gosta do elefante cinza.",
            "Um elefante é maior que um ônibus."
        )
    ),
    "bear" to ConteudoEducacional(
        palavraTraduzida = "Urso",
        silabas = "Ur-so",
        frases = listOf(
            "O urso hiberna no inverno.",
            "Eu vi um urso na floresta.",
            "Lá no Canadá o povo caça urso."
        )
    ),
    "zebra" to ConteudoEducacional(
        palavraTraduzida = "Zebra",
        silabas = "Ze-bra",
        frases = listOf(
            "Eu vi uma zebra no zoológico.",
            "Ela gosta da zebra.",
            "A zebra tem listras pretas e brancas."
        )
    ),
    "giraffe" to ConteudoEducacional(
        palavraTraduzida = "Girafa",
        silabas = "Gi-ra-fa",
        frases = listOf(
            "A girafa tem um pescoço longo.",
            "Eu vi uma girafa no zoológico.",
            "Nunca vi uma girafa na minha vida."
        )
    ),
    "backpack" to ConteudoEducacional(
        palavraTraduzida = "Mochila",
        silabas = "Mo-chi-la",
        frases = listOf(
            "Eu comprei uma mochila nova.",
            "A mochila está pesada.",
            "Minha mochila nova rasgou um dia desses."
        )
    ),
    "umbrella" to ConteudoEducacional(
        palavraTraduzida = "Guarda-chuva",
        silabas = "Guar-da-chu-va",
        frases = listOf(
            "Eu tenho um guarda-chuva.",
            "O guarda-chuva está aberto.",
            "Sempre chove quando esqeço o guarda-chuva."
        )
    ),
    "handbag" to ConteudoEducacional(
        palavraTraduzida = "Bolsa",
        silabas = "Bol-sa",
        frases = listOf(
            "Ela comprou uma bolsa nova.",
            "A bolsa está na cadeira.",
            "Meu marido me deu uma bolsa de presente"
        )
    ),
    "tie" to ConteudoEducacional(
        palavraTraduzida = "Gravata",
        silabas = "Gra-va-ta",
        frases = listOf(
            "A gravata é rosa.",
            "Ele gosta da gravata nova.",
            "O chefe sempre usa uma gravata ridícula."
        )
    ),
    "suitcase" to ConteudoEducacional(
        palavraTraduzida = "Mala",
        silabas = "Ma-la",
        frases = listOf(
            "Eu preparei a mala para viajar.",
            "A mala está pesada.",
            "Quase não achei minha mala no aeroporto."
        )
    ),
    "frisbee" to ConteudoEducacional(
        palavraTraduzida = "Frisbee",
        silabas = "Fris-bee",
        frases = listOf(
            "Eu joguei frisbee no parque.",
            "O frisbee é vermelho.",
            "Ele gosta de brincar com o frisbee."
        )
    ),
    "skis" to ConteudoEducacional(
        palavraTraduzida = "Esquis",
        silabas = "Es-quis",
        frases = listOf(
            "Eu comprei esquis novos.",
            "Os esquis estão na garagem.",
            "Ele gosta de esquiar com os esquis."
        )
    ),
    "snowboard" to ConteudoEducacional(
        palavraTraduzida = "Snowboard",
        silabas = "Snow-board",
        frases = listOf(
            "Eu comprei um snowboard novo.",
            "O snowboard está na loja.",
            "Ele gosta de praticar snowboard."
        )
    ),
    "sports ball" to ConteudoEducacional(
        palavraTraduzida = "Bola",
        silabas = "Bo-la",
        frases = listOf(
            "Eu comprei uma bola.",
            "Perdi bola na quadra.",
            "Nunca deixo de jogar bola com meus amigos."
        )
    ),
    "kite" to ConteudoEducacional(
        palavraTraduzida = "Pipa",
        silabas = "Pi-pa",
        frases = listOf(
            "Eu soltei a pipa no parque.",
            "A pipa está colorida.",
            "Soltei muita pipa quando era mais novo."
        )
    ),
    "baseball bat" to ConteudoEducacional(
        palavraTraduzida = "Taco de beisebol",
        silabas = "Ta-co de bei-se-bol",
        frases = listOf(
            "Eu comprei um taco de beisebol.",
            "O taco de beisebol está na loja.",
            "Ele usa o taco de beisebol para jogar."
        )
    ),
    "baseball glove" to ConteudoEducacional(
        palavraTraduzida = "Luva de beisebol",
        silabas = "Lu-va de bei-se-bol",
        frases = listOf(
            "Eu tenho uma luva de beisebol.",
            "A luva de beisebol está no armário.",
            "Ele usa a luva de beisebol no jogo."
        )
    ),
    "skateboard" to ConteudoEducacional(
        palavraTraduzida = "Skate",
        silabas = "Ska-te",
        frases = listOf(
            "Eu comprei um skate novo.",
            "O skate está na rua.",
            "Meu filho gostava muito de andar de skate."
        )
    ),
    "surfboard" to ConteudoEducacional(
        palavraTraduzida = "Prancha de surf",
        silabas = "Pran-cha de surf",
        frases = listOf(
            "Eu comprei uma prancha de surf.",
            "A prancha de surf está na praia.",
            "A prancha de surf dele quebrou ao meio."
        )
    ),
    "tennis racket" to ConteudoEducacional(
        palavraTraduzida = "Raquete de tênis",
        silabas = "Ra-que-te de tê-nis",
        frases = listOf(
            "Eu comprei uma raquete de tênis.",
            "A raquete de tênis está na quadra.",
            "Ela usa a raquete de tênis guardada lá em casa."
        )
    ),
    "bottle" to ConteudoEducacional(
        palavraTraduzida = "Garrafa",
        silabas = "Gar-ra-fa",
        frases = listOf(
            "Eu tenho uma garrafa rosa.",
            "A garrafa está na mesa.",
            "Nunca deixo de levar uma garrafa de água"
        )
    ),
    "wine glass" to ConteudoEducacional(
        palavraTraduzida = "Taça de vinho",
        silabas = "Ta-ça de vi-nho",
        frases = listOf(
            "Eu comprei uma taça de vinho.",
            "A taça está na mesa.",
            "Sempre abrimos uma taça de vino pra comemorar."
        )
    ),
    "cup" to ConteudoEducacional(
        palavraTraduzida = "Copo",
        silabas = "Co-po",
        frases = listOf(
            "Eu tenho um copo de vidro.",
            "O copo está na cozinha.",
            "Ele usa o copo para beber refrigerante."
        )
    ),
    "fork" to ConteudoEducacional(
        palavraTraduzida = "Garfo",
        silabas = "Gar-fo",
        frases = listOf(
            "Eu uso o garfo para comer.",
            "O garfo está na mesa.",
            "Semana passada perdi o meu garfo."
        )
    ),
    "knife" to ConteudoEducacional(
        palavraTraduzida = "Faca",
        silabas = "Fa-ca",
        frases = listOf(
            "Eu uso a faca para cortar pão.",
            "A faca está afiada.",
            "Ela pegou a faca pra cortar um pão."
        )
    ),
    "spoon" to ConteudoEducacional(
        palavraTraduzida = "Colher",
        silabas = "Co-lhe-r",
        frases = listOf(
            "Eu uso a colher para comer sopa.",
            "A colher está na gaveta.",
            "Esqueceram de lavar a colher ontem."
        )
    ),
    "bowl" to ConteudoEducacional(
        palavraTraduzida = "Tigela",
        silabas = "Ti-ge-la",
        frases = listOf(
            "Eu tenho uma tigela grande.",
            "A tigela está na mesa.",
            "A tigela quebrou quando fui lavar a louça."
        )
    ),
    "banana" to ConteudoEducacional(
        palavraTraduzida = "Banana",
        silabas = "Ba-na-na",
        frases = listOf(
            "Eu comi uma banana.",
            "A banana está madura.",
            "Ela gosta da banana machucada no café da manhã."
        )
    ),
    "apple" to ConteudoEducacional(
        palavraTraduzida = "Maçã",
        silabas = "Ma-çã",
        frases = listOf(
            "Eu comi uma maçã.",
            "A maçã está vermelha.",
            "Ela disse que seu filho gosta muito de maçã."
        )
    ),
    "sandwich" to ConteudoEducacional(
        palavraTraduzida = "Sanduíche",
        silabas = "San-du-í-che",
        frases = listOf(
            "Eu fiz um sanduíche para o almoço.",
            "O sanduíche está na marmita.",
            "Semana passada comi o melhor sanduíche da minha vida."
        )
    ),
    "orange" to ConteudoEducacional(
        palavraTraduzida = "Laranja",
        silabas = "La-ran-ja",
        frases = listOf(
            "Eu comi uma laranja.",
            "A laranja está doce.",
            "Ela não sabe se prefere laranja, tangeria ou mexerica."
        )
    ),
    "broccoli" to ConteudoEducacional(
        palavraTraduzida = "Brócolis",
        silabas = "Bró-co-lis",
        frases = listOf(
            "Eu cozinhei brócolis no jantar.",
            "Os brócolis estão frescos.",
            "Ela gosta dos brócolis na salada da marmita."
        )
    ),
    "carrot" to ConteudoEducacional(
        palavraTraduzida = "Cenoura",
        silabas = "Ce-nou-ra",
        frases = listOf(
            "Eu comprei uma cenoura.",
            "A cenoura está na geladeira.",
            "Ela gosta da cenoura picada na sopa."
        )
    ),
    "hot dog" to ConteudoEducacional(
        palavraTraduzida = "Cachorro-quente",
        silabas = "Ca-chor-ro-quen-te",
        frases = listOf(
            "Eu comi um cachorro-quente.",
            "O cachorro-quente está gostoso.",
            "O cachorro-quente veio bem recheado dessa vez."
        )
    ),
    "pizza" to ConteudoEducacional(
        palavraTraduzida = "Pizza",
        silabas = "Piz-za",
        frases = listOf(
            "Eu comi uma pizza no jantar.",
            "A pizza está quente.",
            "Ela gosta da pizza de calabresa com muito queijo."
        )
    ),
    "donut" to ConteudoEducacional(
        palavraTraduzida = "Rosquinha",
        silabas = "Ros-qui-nha",
        frases = listOf(
            "Eu comi uma rosquinha.",
            "A rosquinha está doce.",
            "Ela gosta da rosquinha que vende no posto."
        )
    ),
    "cake" to ConteudoEducacional(
        palavraTraduzida = "Bolo",
        silabas = "Bo-lo",
        frases = listOf(
            "Eu comi um bolo no aniversário.",
            "O bolo está delicioso.",
            "A mãe e a filha fizeram um bolo gostoso."
        )
    ),
    "couch" to ConteudoEducacional(
        palavraTraduzida = "Sofá",
        silabas = "So-fá",
        frases = listOf(
            "Eu comprei um sofá novo.",
            "O sofá está na sala.",
            "Adoro quando chego em casa e deito no sofá."
        )
    ),
    "potted plant" to ConteudoEducacional(
        palavraTraduzida = "Planta",
        silabas = "Plan-ta",
        frases = listOf(
            "Eu tenho uma planta.",
            "A planta está na janela.",
            "A minha planta é muito especial para mim."
        )
    ),
    "bed" to ConteudoEducacional(
        palavraTraduzida = "Cama",
        silabas = "Ca-ma",
        frases = listOf(
            "Eu comprei uma cama nova.",
            "A cama está arrumada.",
            "Ela cobriu a cama com o lençol novo."
        )
    ),
    "dining table" to ConteudoEducacional(
        palavraTraduzida = "Mesa de jantar",
        silabas = "Me-sa de jan-tar",
        frases = listOf(
            "A mesa de jantar está posta.",
            "Eu limpei a mesa de jantar.",
            "Ela adora quando a mesa de jantar está cheia."
        )
    ),
    "toilet" to ConteudoEducacional(
        palavraTraduzida = "Vaso sanitário",
        silabas = "Va-so sa-ni-tá-rio",
        frases = listOf(
            "O vaso sanitário está limpo.",
            "Eu limpei o vaso sanitário.",
            "O vaso sanitário está entupido desde ontem."
        )
    ),
    "tv" to ConteudoEducacional(
        palavraTraduzida = "Televisão",
        silabas = "Te-le-vi-são",
        frases = listOf(
            "Eu assisti à televisão ontem.",
            "A televisão está ligada.",
            "Ela gosta de ver televisão com sua família."
        )
    ),
    "laptop" to ConteudoEducacional(
        palavraTraduzida = "Notebook",
        silabas = "Note-book",
        frases = listOf(
            "Eu gosto do meu notebook.",
            "O notebook está guardado.",
            "Ele usa o notebook para trabalhar remotamente."
        )
    ),
    "remote" to ConteudoEducacional(
        palavraTraduzida = "Controle remoto",
        silabas = "Con-tro-le re-mo-to",
        frases = listOf(
            "Eu peguei o controle remoto.",
            "O controle remoto está na sala.",
            "Ele usa o controle remoto para aumentar o volume."
        )
    ),
    "cell phone" to ConteudoEducacional(
        palavraTraduzida = "Celular",
        silabas = "Ce-lu-lar",
        frases = listOf(
            "Eu tenho um celular pequeno.",
            "O celular está carregando.",
            "Ele usa o celular para me ligar todos os dias."
        )
    ),
    "microwave" to ConteudoEducacional(
        palavraTraduzida = "Micro-ondas",
        silabas = "Mi-cro-on-das",
        frases = listOf(
            "O micro-ondas está limpo.",
            "Ele gosta do micro-ondas rápido.",
            "Eu usei o micro-ondas para esquentar a comida."
        )
    ),
    "oven" to ConteudoEducacional(
        palavraTraduzida = "Forno",
        silabas = "For-no",
        frases = listOf(
            "Eu assei o bolo no forno.",
            "O forno está quente.",
            "O seu forno é muito potente."
        )
    ),
    "toaster" to ConteudoEducacional(
        palavraTraduzida = "Torradeira",
        silabas = "Tor-ra-dei-ra",
        frases = listOf(
            "Eu comprei uma torradeira nova.",
            "A torradeira está na cozinha.",
            "Uso minha torradeira todo dia de manhã."
        )
    ),
    "sink" to ConteudoEducacional(
        palavraTraduzida = "Pia",
        silabas = "Pi-a",
        frases = listOf(
            "Eu lavei a louça na pia.",
            "A pia está limpa.",
            "Ela concertou a pia que caiu."
        )
    ),
    "refrigerator" to ConteudoEducacional(
        palavraTraduzida = "Geladeira",
        silabas = "Ge-la-dei-ra",
        frases = listOf(
            "Eu abri a geladeira.",
            "A geladeira está cheia.",
            "A geladeira lá de casa quebrou."
        )
    ),
    "book" to ConteudoEducacional(
        palavraTraduzida = "Livro",
        silabas = "Li-vro",
        frases = listOf(
            "Eu li um livro interessante.",
            "O livro está na estante.",
            "Ele lê muito o livro que eu dei."
        )
    ),
    "clock" to ConteudoEducacional(
        palavraTraduzida = "Relógio",
        silabas = "Re-ló-gio",
        frases = listOf(
            "Eu olhei para o relógio.",
            "O relógio está atrasado.",
            "Ela gostou do relógio do meu relógio."
        )
    ),
    "vase" to ConteudoEducacional(
        palavraTraduzida = "Vaso",
        silabas = "Va-so",
        frases = listOf(
            "O vaso está na mesa.",
            "Ela gosta do vaso azul.",
            "Eu comprei um vaso para colocar minhas flores."
        )
    ),
    "scissors" to ConteudoEducacional(
        palavraTraduzida = "Tesoura",
        silabas = "Te-sou-ra",
        frases = listOf(
            "A tesoura está afiada.",
            "Ela gosta da tesoura nova.",
            "Eu usei a tesoura para cortar papel."
        )
    ),
    "teddy bear" to ConteudoEducacional(
        palavraTraduzida = "Urso de pelúcia",
        silabas = "Ur-so de pe-lú-ci-a",
        frases = listOf(
            "Eu tenho um urso de pelúcia.",
            "O urso de pelúcia está na cama.",
            "Minha mãe me deu esse urso de pelúcia."
        )
    ),
    "hair drier" to ConteudoEducacional(
        palavraTraduzida = "Secador de cabelo",
        silabas = "Se-ca-dor de ca-be-lo",
        frases = listOf(
            "Eu usei o secador de cabelo.",
            "O secador de cabelo está no banheiro.",
            "Ele tem um secador de cabelo novo."
        )
    ),
    "toothbrush" to ConteudoEducacional(
        palavraTraduzida = "Escova de dentes",
        silabas = "Es-co-va de den-tes",
        frases = listOf(
            "Eu tenho uma escova de dentes nova.",
            "A escova de dentes está no banheiro.",
            "Ela usa a escova de dentes todos os dias."
        )
    ),
    "keyboard" to ConteudoEducacional(
        palavraTraduzida = "teclado",
        silabas = "Te-cla-do",
        frases = listOf(
            "Ganhei um telcado",
            "O teclado dele fica brilhando",
            "Todo teclado que vejo é mais caro que o anterior",
        )
    ),

    )

/**
 * Chama a API do Gemini ou usa o dicionário local para gerar conteúdo.
 */
suspend fun gerarConteudoEducacional(
    apiKey: String,
    nomeDoObjetoEmIngles: String
): GeminiResult {
    if (dicionarioMock.containsKey(nomeDoObjetoEmIngles)) {
        Log.d("GeminiApp", "Objeto encontrado no dicionário local: $nomeDoObjetoEmIngles")
        delay(500)
        return GeminiResult.Success(dicionarioMock.getValue(nomeDoObjetoEmIngles))
    }

    Log.d("GeminiApp", "Objeto não encontrado no dicionário. Chamando a API do Gemini...")
    // Código do Gemini removido
    return GeminiResult.Error("Objeto não encontrado no dicionário de simulação.")
}
