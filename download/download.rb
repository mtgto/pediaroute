# bundle exec ruby download.rb 20140208

require 'nokogiri'
require 'open-uri'
require 'thor'

class Download < Thor
  desc 'download USER DB', 'download Update wikipedia data and write to mariadb.'
  def download(user, db)
    exec("echo 'CREATE TABLE IF NOT EXISTS version(name char(8));' | mysql -u #{user} #{db}")
    @last_version = `echo 'SELECT name FROM version' | mysql -u #{user} #{db} | tail -n 1`.strip
    doc = Nokogiri::HTML(open('http://dumps.wikimedia.org/jawiki/'))
    latest_version = doc.css('a')[-2].text
    if latest_version == @last_version
      STDERR.puts "すでに最新なのでスキップします"
      exit
    end
    exec('wget --quiet http://dumps.wikimedia.org/jawiki/latest/jawiki-latest-page.sql.gz')
    exec('wget --quiet http://dumps.wikimedia.org/jawiki/latest/jawiki-latest-pagelinks.sql.gz')
    exec("echo 'DROP TABLE IF EXISTS page; DROP TABLE IF EXISTS pagelinks;' | mysql -u #{user} #{db}")
    exec("gzcat jawiki-latest-page.sql.gz | time mysql -u #{user} #{db}")
    exec("gzcat jawiki-latest-pagelinks.sql.gz | time mysql -u #{user} #{db}")
    exec('rm jawiki-latest-page.sql.gz')
    exec('rm jawiki-latest-pagelinks.sql.gz')
    if @last_version.empty?
      exec("echo 'INSERT INTO version VALUES(#{latest_version});' | mysql -u #{user} #{db}") 
    else
      exec("echo 'UPDATE version SET name = #{latest_version};' | mysql -u #{user} #{db}")
    end
  end

  private
  def exec(cmd)
    STDERR.puts "executing \"#{cmd}\""
    exit(1) unless system(cmd)
  end
end

Download.start(ARGV)

