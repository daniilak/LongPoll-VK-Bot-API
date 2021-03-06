package VK;

import Entities.Server;
import Lib.Request;
import Objects.Keyboard;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Objects;

/**
 * Класс работы c VK API.
 * @autor daniilak
 * @version 1.1
 */
public class VK {

    /** Поле номер сообщества */
    private Integer groupID;

    /** Поле объект LongPoll сервера */
    private Server srv;

    /** Поле пользовательский ключ доступа */
    private String userToken;

    /** Поле ключ доступа сообщества */
    private String botToken;

    /** Поле версия API VK */
    private double version = 5.92;

    /**
     * Конструктор - создание нового объекта работы с VK API
     * @param groupID - номер сообщества
     * @param userToken - пользовательский ключ доступа
     * @param botToken - ключ доступа сообщества
     */
    public VK(Integer groupID, String userToken, String botToken) {

        this.groupID = groupID;
        this.userToken = userToken;
        this.botToken = botToken;

        JSONObject srvTemp = this.getLongPollServer();
        this.srv  = new Server(srvTemp.getString("server"), srvTemp.getString("key"), srvTemp.getInt("ts"));
    }

    /**
     * Функция получения данных LongPoll сервера VK
     * @return возвращает JSON строку с данными LongPoll сервера VK
     */
    private JSONObject getLongPollServer() {
        return this.queryFromUser("groups.getLongPollServer", "group_id=" + this.groupID)
                .getJSONObject("response");
    }

    /**
     * Функция получения данных API при помощи пользовательского ключа
     * @return возвращает JSONObject
     */
    public JSONObject queryFromUser(String method, String params) {
        params = "access_token=" + this.userToken + "&" + "version=" + version + "&" + params;
        return Request.query(method,params);
    }

    /**
     * Функция получения
     * @return возвращает JSONObject
     */
    public JSONObject queryFromBot(String method, String params) {
        params = "access_token=" + this.botToken+ "&" + "version=" + version + "&" + params;
        return Request.query(method,params);
    }

    /**
     * Функция получения обновлений при помощи LongPoll
     * @param wait - задержка в секундах
     */
    public void getUpdates(Integer wait) {

        JSONObject json, rec;
        JSONArray updates;

        while (true) {
            json = new JSONObject(
                    Objects.requireNonNull(
                            Request.postLongPoll(this.srv.getServer()+ "?act=a_check&key=" + this.srv.getKey() + "&ts=" + this.srv.getTs() + "&wait=" + wait + "&mode=2&version=2")
                    )
            );


            if (json.has("failed")) {
                System.out.println("Response from VK API: failed");
                return;
            }

            updates = json.getJSONArray("updates");

            if (updates.length() != 0) {
                this.srv.setTs(json.getInt("ts"));
                for (int i = 0; i < updates.length(); ++i) {
                    rec = updates.getJSONObject(i);
                    this.parserType(rec.getJSONObject("object"), rec.getString("type"));
                }
            }
        }
    }

    /**
     * Функция парсера разных типов уведомлений сообщества, см. https://vk.com/dev/groups_events
     * @param object - JSONObject уведомления
     * @param type - тип уведомления, см
     */
    public void parserType(JSONObject object, String type) {
        switch (type) {
            case "message_typing_state":
                System.out.println("id" + object.getInt("from_id") + " is " + object.getString("state"));
                break;
            case "message_new":
                System.out.println("Сообщение «" + object.getString("text") + "» от id" + object.getInt("from_id") + " дата " + new java.util.Date((long)object.getInt("date")*1000));

                if (object.has("reply_message")) {
                    System.out.println("Есть пересланные сообщения");
                }
                if (object.has("payload")){
                    System.out.println("Пользователь нажал кнопку «" + object.getString("text") + "», с параметром «" + object.getString("payload") + "»");
                }

                /** Добавление клавиатуры */
                Keyboard keyboard = new Keyboard(false);
                keyboard.addButton("text button", "negative", "Красная кнопка");
                keyboard.addButton("text button", "positive", "Зеленая кнопка");
                keyboard.addButton("text button", "default", "Белая кнопка");
                keyboard.addButton("text button", "primary", "Синяя кнопка");

                /** Отправление запроса "Отправить сообщение" с параметрами в ответ польователю
                 * вместе с параметром keyboard*/
                this.queryFromBot("messages.send","user_id=" + object.getInt("from_id") + "&message="+ object.getString("text") + " пикачу&keyboard=" + keyboard.getJSON() );
                break;
            case "message_reply":
                System.out.println("Ответное сообщение «" + object.getString("text") + "» для id" + object.getInt("peer_id"));
                break;
            case "message_edit":
                break;
            case "message_allow":
                break;
            case "message_deny":
                break;
            case "photo_new":
                break;
            case "photo_comment_new":
                break;
            case "photo_comment_edit":
                break;
            case "photo_comment_restore":
                break;
            case "photo_comment_delete":
                break;
            case "audio_new":
                break;
            case "video_new":
                break;
            case "video_comment_new":
                break;
            case "video_comment_edit":
                break;
            case "video_comment_restore":
                break;
            case "video_comment_delete":
                break;
            case "wall_post_new":
                System.out.println("Новая запись «" + object.getString("text") + "» дата в UnixTime" + object.getInt("date"));
                break;
            case "wall_repost":
                break;
            case "wall_reply_new":
                break;
            case "wall_reply_edit":
                break;
            case "wall_reply_restore":
                break;
            case "wall_reply_delete":
                break;
            case "board_post_new":
                break;
            case "board_post_edit":
                break;
            case "board_post_restore":
                break;
            case "board_post_delete":
                break;
            case "market_comment_new":
                break;
            case "market_comment_edit":
                break;
            case "market_comment_restore":
                break;
            case "market_comment_delete":
                break;
            case "group_leave":
                break;
            case "group_join":
                break;
            case "user_block":
                break;
            case "user_unblock":
                break;
            case "poll_vote_new":
                break;
            case "group_officers_edit":
                break;
            case "group_change_settings":
                break;
            case "group_change_photo":
                break;
        }
        // Вывод содержимого json, которое присылает VK API в ответ на различные действия (см выше)
//        System.out.println(object);
    }
}
